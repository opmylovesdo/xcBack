package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/6
 * @Description: com.xuecheng.manage_media_process.mq
 * @version: 1.0
 */
@Component
public class MediaProcessTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);

    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpegPath;

    //上传文件的根目录
    @Value("${xc-service-manage-media.upload-location}")
    private String serverPath;

    @Autowired
    private MediaFileRepository mediaFileRepository;


    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}", containerFactory = "customContainerFactory")
    public  void receiveMediaProcessTask(String msg) throws IOException {
        Map msgMap = JSON.parseObject(msg, Map.class);
        LOGGER.info("receive media process task msg:{}", msgMap);
        //解析消息
        //获取媒资文件id
        String mediaId = (String) msgMap.get("mediaId");
        //获取媒资文件信息
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        if(!optionalMediaFile.isPresent()){
            return ;
        }

        MediaFile mediaFile = optionalMediaFile.get();
        /*
            获取媒资类型
                目前只处理avi类型
                非avi类型 设置媒资文件状态为303004(无需处理) 保存到MongoDB
                为avi类型 设置媒资文件状态为303001(处理中) 保存到MongoDB
         */
        String fileType = mediaFile.getFileType();
        if(StringUtils.isNotEmpty(fileType) && "avi".equalsIgnoreCase(fileType)){
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        }else{
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }

        //生成MP4(avi -> mp4)
        String videoPath = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4Name = mediaFile.getFileId() + ".mp4";
        String mp4FolderPath = serverPath + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath, videoPath, mp4Name, mp4FolderPath);
        String result = mp4VideoUtil.generateMp4();
        //操作失败
        if(StringUtils.isEmpty(result) || !"success".equalsIgnoreCase(result)){
            processFail(mediaFile, result);
            return ;
        }

        //操作成功 生成m3u8
        videoPath = mp4FolderPath + mp4Name; //此地址为mp4地址
        String m3u8Name = mediaFile.getFileId() + ".m3u8";
        String m3u8FolderPath = mp4FolderPath + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpegPath, videoPath, m3u8Name, m3u8FolderPath);
        result = hlsVideoUtil.generateM3u8();
        if(null == result || !"success".equalsIgnoreCase(result)){
            //操作失败写入处理日志
            processFail(mediaFile, result);
            return ;
        }

        //获取m3u8列表
        List<String> tsList = hlsVideoUtil.get_ts_list();
        //更新处理状态为成功
        mediaFile.setProcessStatus("303002");//处理状态为成功
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(tsList);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //m3u8文件url(就是视频播放的相对路径)
        mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + m3u8Name);
        mediaFileRepository.save(mediaFile);
    }

    private void processFail(MediaFile mediaFile, String result) {
        //写入处理日志
        mediaFile.setProcessStatus("303003");//处理状态为处理失败
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setErrormsg(result);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        mediaFileRepository.save(mediaFile);
    }

}
