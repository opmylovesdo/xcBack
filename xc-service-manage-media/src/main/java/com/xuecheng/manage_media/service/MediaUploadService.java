package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.omg.CORBA.COMM_FAILURE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;


/**
 * @Auther: LUOLUO
 * @Date: 2019/6/4
 * @Description: com.xuecheng.manage_media.service
 * @version: 1.0
 */
@Service
public class MediaUploadService {
    public static final Logger LOGGER = LoggerFactory.getLogger(MediaUploadService.class);

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //上传文件根路径
    @Value("${xc-service-manage-media.upload-location}")
    private String uploadPath;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;


    /**
     * 文件上传前的检查工作,判断文件是否上传
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimeType
     * @param fileExt
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimeType, String fileExt) {

        //1检查文件在磁盘上是否存在
        //文件目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件的路径
        String filePath = this.getFilePath(fileMd5, fileExt);

        File file = new File(filePath);
        boolean exists = file.exists();

        //2检查文件信息在mongodb中是否存在
        Optional<MediaFile> mediaFile = mediaFileRepository.findById(fileMd5);
        mediaFile.filter(t -> exists)
                .ifPresent((t) -> ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST));

        //文件不存在
        //做一些准备工作, 检查文件目录是否存在  如果不存在 则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }


        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     *
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt) {
        return getFileFolderBasePath(fileMd5)
                .append(fileMd5).append(".").append(fileExt)
                .toString();
    }

    /**
     * 获取文件所在文件夹的路径 包装的StringBuilder对象
     *
     * @param fileMd5
     * @return
     */
    private StringBuilder getFileFolderBasePath(String fileMd5) {
        return new StringBuilder()
                .append(uploadPath)
                .append(fileMd5.charAt(0)).append("/")
                .append(fileMd5.charAt(1)).append("/")
                .append(fileMd5).append("/");
    }

    /**
     * 获取文件的相对路径
     *
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getChunkFileRelativePath(String fileMd5, String fileExt) {
        return new StringBuilder()
                .append(fileMd5.charAt(0)).append("/")
                .append(fileMd5.charAt(1)).append("/")
                .append(fileMd5).append("/")
//                .append(fileMd5).append(".").append(fileExt)
                .toString();
    }

    /**
     * 获取文件所在文件夹的路径
     *
     * @param fileMd5
     * @return
     */
    private String getFileFolderPath(String fileMd5) {
        return getFileFolderBasePath(fileMd5)
                .toString();
    }

    /**
     * 获取分块文件所在文件夹的位置
     *
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return getFileFolderBasePath(fileMd5)
                .append("chunk").append("/")
                .toString();
    }

    /**
     * 检查块文件是否存在
     *
     * @param fileMd5   文件md5
     * @param chunk     块的下标
     * @param chunkSize 块的大小
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFilePath = this.getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFilePath + chunk);

        //块文件不存在
        if (!chunkFile.exists()) {
            return new CheckChunkResult(CommonCode.SUCCESS, false);
        }

        /*
            块文件存在 但是有可能不完整
            若块文件大小小于块大小
            情况1. 文件不完整(不是最后一块) 删除重新上传
            情况2. 最后一块(若是断点重传,最后一块很可能也不完整) 删除重新上传
         */
        if (chunkFile.length() < chunkSize) {
            chunkFile.delete();
            return new CheckChunkResult(CommonCode.SUCCESS, false);
        }

        return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
    }

    /**
     * 上传分块
     *
     * @param file    分块文件
     * @param chunk   分块下标
     * @param fileMd5 文件md5
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {

        //检查分块的目录, 若存在自动创建
        //得到分块目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;

        File chunkFileFolder = new File(chunkFileFolderPath);
        //如果不存在则创建
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }

        //得到上传文件的输入流
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(new File(chunkFilePath))) {

            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并分块
     *
     * @param fileMd5  文件md5
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param mimeType MIME类型
     * @param fileExt  文件拓展名
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimeType, String fileExt) {

        //1.合并所有分块
        //得到分块文件目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File file = new File(chunkFileFolderPath);
        //分块文件列表
        File[] chunkFiles = file.listFiles();

        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

        //合并文件
        Optional<File> mergeFileOptional = this.mergeFile(Arrays.asList(chunkFiles), mergeFile);
        if (!mergeFileOptional.isPresent()) {
            //合并文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }


        //2.检查合并后文件MD5和前端传来的MD5是否一致
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //3.将文件信息写入MongoDB
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        //绝对路径:D:\XueCheng\video\chunk\a\b\md5\
        //相对路径: a\b\md5\
        mediaFile.setFilePath(this.getChunkFileRelativePath(fileMd5, fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimeType);
        mediaFile.setFileType(fileExt);

        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);

        //发送消息
        this.sendProcessVideoMsg(save.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 向MQ发送视频处理信息
     *
     * @param mediaId 视频ID
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId) {
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        if (!optionalMediaFile.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = optionalMediaFile.get();
        //发送视频处理消息
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaId);
        //发送的消息
        String msg = JSON.toJSONString(msgMap);

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msg);
            LOGGER.info("send media process task msg:{}", msg);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("send media process task error,msg is:{},error:{}", msg, e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }


        return ResponseResult.SUCCESS();
    }

    /**
     * 检查合并文件的MD5和传入MD5是否一致
     *
     * @param mergeFile
     * @param md5
     * @return
     */
    private boolean checkFileMd5(File mergeFile, String md5) {

        try (FileInputStream inputStream = new FileInputStream(mergeFile)) {
            //得到文件的md5
            String md5Hex = DigestUtils.md5Hex(inputStream);

            //和传入的md5比较
            if (md5.equalsIgnoreCase(md5Hex)) {
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    /**
     * 合并文件
     *
     * @param chunkFileList
     * @param mergeFile
     * @return
     */
    private Optional<File> mergeFile(List<File> chunkFileList, File mergeFile) {

        try {
            //如果文件存在 先删除
            if (mergeFile.exists()) {
                mergeFile.delete();
            }

            //创建一个新文件
            mergeFile.createNewFile();

            //对块文件进行排序
            chunkFileList.sort(Comparator.comparingInt(f -> Integer.parseInt(f.getName())));

            //创建一个写对象
            try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw")) {

                //创建缓冲区
                byte[] b = new byte[1024];
                for (File chunkFile : chunkFileList) {
                    try (RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r")) {
                        int len = -1;
                        while ((len = raf_read.read(b)) != -1) {
                            raf_write.write(b, 0, len);
                        }
                    }

                }
            }

            return Optional.ofNullable(mergeFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }
}
