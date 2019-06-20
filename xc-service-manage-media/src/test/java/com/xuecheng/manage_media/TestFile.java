package com.xuecheng.manage_media;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Auther: LUOLUO
 * @Date: 2019/6/3
 * @Description: com.xuecheng.manage_media
 * @version: 1.0
 */
public class TestFile {

    public static void main(String[] args) {
//        IntStream.range(0, 4).forEach(i -> {
//            new Thread(() -> {
//                while(true){
//                    System.out.println(Thread.currentThread().getName() + i + " is running");
//                }
//            }).start();
//        });
        while(true){
            System.out.println(Thread.currentThread().getName()  + " is running");
        }
    }


    @Test
    public void testChunk() throws IOException {
        File sourseFile = new File("D:\\XueCheng\\video\\lucene.mp4");
        String chunkPath = "D:\\XueCheng\\video\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        //分块大小
        long chunkSize = 1024 * 1024 * 1;
        //分块数量
        long chunkNum = (long) Math.ceil(sourseFile.length() * 1.0 / chunkSize);
        if (chunkNum <= 0) {
            chunkNum = 1;
        }

        //缓冲区大小
        byte[] b = new byte[1024];
        //使用RandomAccessFile访问文件
        RandomAccessFile raf_read = new RandomAccessFile(sourseFile, "r");
        //分块
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File file = new File(chunkPath + i);
            boolean newFile = file.createNewFile();
            if (newFile) {
                //向分块文件中写数据
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                    if (file.length() > chunkSize) {
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();
    }


    @Test
    public void testMergeFile() throws IOException {
        //块文件目录
        File chunkFolder = new File("D:\\XueCheng\\video\\chunk");
        //合并文件
        File mergeFile = new File("D:\\XueCheng\\video\\lucene1.mp4");

        if (mergeFile.exists()) {
            mergeFile.delete();
        }

        //创建新的合并文件
        boolean newFile = mergeFile.createNewFile();
        //用于写文件
        try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw")) {
            //指针指向文件顶端
            raf_write.seek(0);
            //缓存区
            byte[] b = new byte[1024];
            //从小到大排序
            List<File> fileList = Arrays.asList(chunkFolder.listFiles());
            fileList.sort(Comparator.comparingInt((f -> -Integer.parseInt(f.getName()))));

            System.out.println(fileList);
            //合并文件
//            for (File chunkFile : fileList) {
//                try (RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r")) {
//                    int len = -1;
//                    while ((len = raf_read.read(b)) != -1) {
//                        raf_write.write(b, 0, len);
//                    }
//                }
//            }
        }

    }


    @Test
    public void testStream(){
        List<String> list = null;
        list.stream().forEach(System.out::println);
    }


}
