package com.jiubuntu.wms.global.infrastructure;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String upload(MultipartFile file, String directory);

    String getUrl(String key);

}
