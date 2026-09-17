package org.txf.myblogsprinboot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        return vo;
    }

}
