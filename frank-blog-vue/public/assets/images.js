/* 原型图片保存在 IndexedDB；正式项目用 SSM 上传接口替换 put/get。 */
window.blogImages = (() => {
  let database;
  const urls = new Map();
  const open = () => database || (database = new Promise((resolve, reject) => {
    if (!window.indexedDB) return reject(new Error('浏览器不支持本地图片存储'));
    const request = indexedDB.open('frank-blog-images', 1);
    request.onupgradeneeded = () => request.result.createObjectStore('images');
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => { database = null; reject(new Error('无法打开本地图片存储')); };
  }));
  async function put(file, id, attachment = false) {
    if (!attachment && !['image/png','image/jpeg','image/webp','image/gif'].includes(file.type)) throw new Error('请选择 PNG、JPG、WebP 或 GIF 图片');
    if (file.size > 20 * 1024 * 1024) throw new Error('单个文件请控制在 20 MB 以内');
    if (!attachment) {
    const testUrl = URL.createObjectURL(file);
    try {
      await new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = resolve;
        img.onerror = () => reject(new Error('这张图片无法读取，请换一张重试'));
        img.src = testUrl;
      });
    } finally { URL.revokeObjectURL(testUrl); }
    }
    const db = await open();
    await new Promise((resolve, reject) => {
      const tx = db.transaction('images', 'readwrite');
      tx.objectStore('images').put(attachment ? {blob:file,name:file.name||'附件'} : file, id);
      tx.oncomplete = resolve;
      tx.onerror = tx.onabort = () => reject(new Error('文件保存失败，本地存储空间可能不足'));
    });
    return id;
  }
  async function getUrl(id) {
    if (urls.has(id)) return urls.get(id);
    const db = await open();
    const file = await readFile(db, id);
    if (!file) throw new Error('本地文件不存在');
    if (!urls.has(id)) urls.set(id, URL.createObjectURL(file));
    return urls.get(id);
  }
  function readFile(db, id) {
    return new Promise((resolve, reject) => {
      const request = db.transaction('images').objectStore('images').get(id);
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(new Error('读取图片失败'));
    });
  }
  async function hydrate(container) {
    await Promise.all([...container.querySelectorAll('img[data-local-image]')].map(async img => {
      try { const url = await getUrl(img.dataset.localImage); if (img.isConnected) img.src = url; }
      catch { if (img.isConnected) { const missing = document.createElement('span'); missing.className='missing-image'; missing.textContent='图片无法读取：请在添加图片时使用的浏览器中查看'; img.replaceWith(missing); } }
    }));
    await Promise.all([...container.querySelectorAll('a[data-local-file]')].map(async link => {
      try {
        const id = link.dataset.localFile, key = 'download-' + id;
        const file = await readFile(await open(), id);
        if (!file) throw new Error('文件不存在');
        if (!urls.has(key)) urls.set(key, URL.createObjectURL(new Blob([file.blob||file], {type:'application/octet-stream'})));
        if (link.isConnected) { link.href=urls.get(key);link.download=file.name||'附件';link.classList.add('attachment-link');link.title='下载附件：'+(file.name||'文件'); }
      } catch { if(link.isConnected){link.removeAttribute('href');link.classList.add('missing-image');link.textContent+='（本地附件不可用）';} }
    }));
  }
  async function remove(id) {
    const db = await open();
    await new Promise((resolve, reject) => {
      const tx = db.transaction('images', 'readwrite');
      tx.objectStore('images').delete(id);
      tx.oncomplete = resolve;
      tx.onerror = tx.onabort = () => reject(new Error('删除本地图片失败'));
    });
    if (urls.has(id)) { URL.revokeObjectURL(urls.get(id)); urls.delete(id); }
  }
  addEventListener('pagehide', () => { for (const url of urls.values()) URL.revokeObjectURL(url); urls.clear(); });
  addEventListener('pageshow', () => hydrate(document));
  return { put, putAttachment: (file,id) => put(file,id,true), getUrl, remove, hydrate };
})();
