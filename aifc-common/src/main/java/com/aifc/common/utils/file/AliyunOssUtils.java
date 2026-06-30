package com.aifc.common.utils.file;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aifc.common.config.AliyunOssProperties;
import com.aifc.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AliyunOssUtils
{
    @Autowired
    private AliyunOssProperties ossProperties;

    public boolean enabled()
    {
        return StringUtils.isNotEmpty(ossProperties.getEndpoint())
            && StringUtils.isNotEmpty(ossProperties.getBucketName())
            && StringUtils.isNotEmpty(ossProperties.getAccessKeyId())
            && StringUtils.isNotEmpty(ossProperties.getAccessKeySecret());
    }

    public String upload(MultipartFile file) throws Exception
    {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.isNotEmpty(originalName) && originalName.contains("."))
        {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String key = trimSlash(ossProperties.getDir()) + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        OSS ossClient = null;
        try (InputStream in = file.getInputStream())
        {
            ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
            ossClient.putObject(ossProperties.getBucketName(), key, in);
            return buildPublicUrl(key);
        }
        finally
        {
            if (ossClient != null)
            {
                ossClient.shutdown();
            }
        }
    }

    public String uploadBytes(String originalName, byte[] content) throws Exception
    {
        String ext = "";
        if (StringUtils.isNotEmpty(originalName) && originalName.contains("."))
        {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String key = trimSlash(ossProperties.getDir()) + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        OSS ossClient = null;
        try
        {
            ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
            ossClient.putObject(ossProperties.getBucketName(), key, new java.io.ByteArrayInputStream(content));
            return buildPublicUrl(key);
        }
        finally
        {
            if (ossClient != null)
            {
                ossClient.shutdown();
            }
        }
    }

    private String buildPublicUrl(String key)
    {
        String endpoint = ossProperties.getEndpoint();
        if (endpoint.startsWith("https://"))
        {
            endpoint = endpoint.substring("https://".length());
        }
        else if (endpoint.startsWith("http://"))
        {
            endpoint = endpoint.substring("http://".length());
        }
        return "https://" + ossProperties.getBucketName() + "." + endpoint + "/" + key;
    }

    private String trimSlash(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "upload";
        }
        String result = value;
        while (result.startsWith("/"))
        {
            result = result.substring(1);
        }
        while (result.endsWith("/"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
