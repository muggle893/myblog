package org.txf.myblogsprinboot.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.txf.myblogsprinboot.constant.AssetKind;
import org.txf.myblogsprinboot.constant.AssetStatus;
import org.txf.myblogsprinboot.constant.StorageProvider;
import org.txf.myblogsprinboot.dao.AssetMapper;
import org.txf.myblogsprinboot.exception.AssetException;
import org.txf.myblogsprinboot.exception.ParamErrorException;
import org.txf.myblogsprinboot.model.Asset;
import org.txf.myblogsprinboot.vo.AssetUploadVO;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Asset服务类，用来处理文件上传等逻辑
 */

@Service
@Slf4j
public class AssetService {
    /**
     * 处理文件上传的具体逻辑
     * @param file  文件
     * @param userId    上传者的id
     * @return AssetUploadVO对象
     */
    @Value("${storage.local.root-path}")
    private String rootPath;
    @Value("${storage.bucket}")
    private String bucket;
    @Autowired
    AssetMapper assetMapper;

    public AssetUploadVO upload(MultipartFile file, Long userId) {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            log.error("文件为空!!!");
            throw new ParamErrorException("上传文件不能为空");
        }

        // userId 由 Controller 从登录 Session 中获取
        // 2. 获取基本信息
        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName)) {
            originalName = "未命名文件";
        }

        // 暂时记录客户端声明的类型，后续补充文件内容校验
        String mediaType = file.getContentType();
        if (!StringUtils.hasText(mediaType)) {
            mediaType = "application/octet-stream";
        }

        // 3. 生成文件标识和存储名称
        String publicId = UUID.randomUUID().toString();

        // 磁盘上的文件可以没有扩展名，原始文件名另外保存在数据库中
        String objectKey = publicId;

        Path directory = Path.of(rootPath).toAbsolutePath();
        Path targetPath = directory.resolve(objectKey);

        // 4. 保存文件到本地
        try {
            Files.createDirectories(directory);
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new UncheckedIOException("文件保存失败", e);
        }

        // 5. 组装文件元数据
        Asset asset = new Asset();
        asset.setPublicId(publicId);
        asset.setUploaderId(userId);
        asset.setOriginalName(originalName);
        asset.setMediaType(mediaType);
        asset.setAssetKind(
                mediaType.startsWith("image/") ? AssetKind.IMAGE : AssetKind.FILE
        );
        asset.setSizeBytes(file.getSize());
        asset.setStorageProvider(StorageProvider.LOCAL);
        asset.setBucket(bucket);
        asset.setObjectKey(objectKey);
        asset.setStatus(AssetStatus.READY);
        asset.setRowVersion(0);

        // sha256 暂时不设置
        // createdAt、updatedAt 使用数据库默认值

        // 6. 保存元数据，并回填数据库生成的主键
        int affectedRows = assetMapper.insert(asset);
        if (affectedRows <= 0) {
            log.error("文件上传失败.");
            throw new AssetException("文件上传失败.");
        }
        // 7. 返回上传结果
        AssetUploadVO vo = new AssetUploadVO();
        vo.setAssetId(asset.getId());
        vo.setPublicId(publicId);
        vo.setOriginalName(originalName);
        vo.setAssetKind(asset.getAssetKind());
        if (vo.getAssetKind().equals(AssetKind.IMAGE)) {
            vo.setUrl("/asset/content/" + asset.getPublicId());
        } else {
            vo.setUrl("/asset/download/" + asset.getPublicId());
        }
        return vo;
    }

    /**
     * 加载图片或者附件
     * @param response http响应
     * @param publicId 资源的公共id
     */
    @SneakyThrows
    public void loadAsset(HttpServletResponse response, String publicId) {
        log.info("资源公共id：" + publicId);
        // 从数据库中查找此资源，不存在的话就告诉前端资源不存在
        Asset asset = assetMapper.selectAssetByPublicId(publicId);
        if (asset == null || asset.getId() == null) {
            log.error("资源不存在, 数据库查找失败");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 根据资源的objectKey拼接路径
        Path rp = Path.of(rootPath);
        Path path = rp.resolve(asset.getObjectKey());

        // 判断这个资源是不是正常的文件类型，不能是文件夹
        if (!Files.isRegularFile(path)) {
            log.error("资源不存在, 文件类型错误.");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 根据图片或者附件设置content-type
        // 再设置Content-disposition
        // 把Content-disposition放到响应头中
        if (AssetKind.IMAGE.equals(asset.getAssetKind())) {
            response.setContentType(asset.getMediaType());
            response.setHeader("Content-Disposition", "inline");
        } else {
            response.setContentType("application/octet-stream");
            // 提示浏览器下载，并设置用户保存时看到的文件名
            String disposition = ContentDisposition.attachment()
                    .filename(asset.getOriginalName(), StandardCharsets.UTF_8)
                    .build()
                    .toString();
            response.setHeader("Content-Disposition", disposition);
        }

        // 设置contentLengthLong，告诉浏览器这个文件有多少个字节
        response.setContentLengthLong(Files.size(path));

        // 调用Files的copy方法把文件写入到响应的输出流中
        Files.copy(path, response.getOutputStream());
    }
    
}
