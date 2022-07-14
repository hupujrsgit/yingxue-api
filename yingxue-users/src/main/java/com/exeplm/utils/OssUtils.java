package com.exeplm.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
@Slf4j
public class OssUtils {


    static final String accessKeyIds = "";
    static final String accessKeySecrets = "";

    // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
    static final String endpoint = "https://oss-cn-shanghai.aliyuncs.com";
    static final String endpointlo = "oss-cn-shanghai.aliyuncs.com";

    private static OSS OssClient=new OSSClientBuilder().build(endpoint, accessKeyIds, accessKeySecrets);

    public OssUtils() {
    }

    /**
     * 获取阿里云OSS客户端对象
     *
     * @return ossClient
     */
    public static OSS getOSSClient() {
        return  OssClient;
    }

    /**
     * 创建存储空间
     *
     * @param
     * @param bucketName 存储空间名
     * @return
     */
    public static String createBucketName( String bucketName) {
        OSS ossClient = getOSSClient();
        // 存储空间
        final String bucketNames = bucketName;
        if (!ossClient.doesBucketExist(bucketName)) {
            // 创建存储空间
            Bucket bucket = ossClient.createBucket(bucketName);
            log.info("创建存储空间成功");
            return bucket.getName();
        }
        return bucketNames;
    }

    /**
     * 删除存储空间buckName
     *
     * @param
     * @param bucketName 存储空间
     */
    public static void deleteBucket( String bucketName) {
        OSS ossClient = getOSSClient();

        ossClient.deleteBucket(bucketName);
        log.info("删除" + bucketName + "Bucket成功");
    }

    /**
     * 根据key删除OSS服务器上的文件
     *
     * @param
     * @param bucketName 存储空间
     * @param folder 模拟文件夹名
     * @param key Bucket下的文件的路径名+文件名 如："upload/cake.jpg"
     */
    public static void deleteFile( String bucketName, String folder, String key) {
        OSS ossClient = getOSSClient();
        ossClient.deleteObject(bucketName, folder + key);
        log.info("删除" + bucketName + "下的文件" + folder + key + "成功");
    }

    /**
     * 创建模拟文件夹
     *
     * @param
     * @param bucketName 存储空间
     * @param folder 模拟文件夹名如
     * @return 文件夹名
     */
    public static String createFolder( String bucketName, String folder) {
        OSS ossClient = getOSSClient();

        // 文件夹名
        final String keySuffixWithSlash = folder;
        // 判断文件夹是否存在，不存在则创建
        if (!ossClient.doesObjectExist(bucketName, keySuffixWithSlash)) {
            // 创建文件夹
            ossClient.putObject(bucketName, keySuffixWithSlash, new ByteArrayInputStream(new byte[0]));
            log.info("创建文件夹成功");
            // 得到文件夹名
            OSSObject object = ossClient.getObject(bucketName, keySuffixWithSlash);
            String fileDir = object.getKey();
            return fileDir;
        }
        return keySuffixWithSlash;
    }





    /**
     * 通过文件名判断并获取OSS服务文件上传时文件的contentType
     *
     * @param fileName 文件名
     * @return 文件的contentType
     */
    public static String getContentType(String fileName) {
        // 文件的后缀名
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if (".bmp".equalsIgnoreCase(fileExtension)) {
            return "image/bmp";
        }
        if (".gif".equalsIgnoreCase(fileExtension)) {
            return "image/gif";
        }
        if (".jpeg".equalsIgnoreCase(fileExtension) || ".jpg".equalsIgnoreCase(fileExtension)
                || ".png".equalsIgnoreCase(fileExtension)) {
            return "image/jpeg";
        }
        if (".png".equalsIgnoreCase(fileExtension)) {
            return "image/png";
        }
        if (".html".equalsIgnoreCase(fileExtension)) {
            return "text/html";
        }
        if (".txt".equalsIgnoreCase(fileExtension)) {
            return "text/plain";
        }
        if (".vsd".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.visio";
        }
        if (".ppt".equalsIgnoreCase(fileExtension) || "pptx".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        if (".doc".equalsIgnoreCase(fileExtension) || "docx".equalsIgnoreCase(fileExtension)) {
            return "application/msword";
        }
        if (".xml".equalsIgnoreCase(fileExtension)) {
            return "text/xml";
        }
        // 默认返回类型
        return "";
    }

    /**
     * 上传图片至OSS
     *
     * @param
     * @param stream 上传文件（文件全路径如：D:\\image\\cake.jpg）
     * @param fileName 文件名称
     * @param folder 要存储的文件夹名
     * @return String 返回文件url
     */
    public static String uploadOss(InputStream stream, String fileName,String folder){

        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = accessKeyIds;
        String accessKeySecret = accessKeySecrets;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "uploadexample";
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
         fileName = "yinxue/"+folder+"/"+fileName;
         //创建文件url地址
         String  resultStr = "http://"+bucketName+"."+endpointlo+"/"+fileName;
        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        OSS ossClient =getOSSClient();
        PutObjectResult putObjectResult = null;
        try {
            //上传到Oss
            putObjectResult = ossClient.putObject(bucketName, fileName, stream);

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());

        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return resultStr;
    }
}
