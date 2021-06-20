package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 实现FastDFS文件管理
 *  文件上传
 *  文件删除
 *  文件下载
 *  文件信息获取
 *  Storage信息获取
 *  Tracker信息获取
 */
public class FastDFSUtil {

    /**
     * 加载Tracker连接信息
     */
    static {
        try{
            //查找classpath下的文件路径
            String filename = new ClassPathResource("fdfs_client.conf").getPath();
            //加载Tracker链接信息
            ClientGlobal.init(filename);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param fastDFSFile: 上传的文件的信息封装
     * @return String[]
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{
        //附加参数
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", fastDFSFile.getAuthor());
        //获取TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        /**
         * 通过StorageClient访问Storage，实现文件上传，并获取文件上传后的存储信息
         * 1. 上传的文件的字节数组
         * 2. 文件的扩展名 ex：jpg
         * 3. 附加参数 ex：作者
         *
         * uploads[] 两个参数
         *  1. uploads[0] 文件上传所存储的Storage的组名字
         *  2. uploads[1] 文件存储到Storage上的名字
         */
        String[] uploads = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        return uploads;
    }

    /**
     * 获取文件信息
     * @param groupName : 组名
     * @param remoteFileName : 文件的存储路径名字
     * @return FileInfo
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception {
        //获取TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        //获取文件信息
        return storageClient.get_file_info(groupName, remoteFileName);
    }

    /**
     * 文件下载
     * @param groupName : 组名
     * @param remoteFileName : 文件的存储路径名字
     * @throws Exception
     * @return InputStream
     */
    public static InputStream downloadFile(String groupName, String remoteFileName) throws Exception{
        //获取TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        //文件下载
        byte[] buffer = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(buffer);
    }

    /**
     * 文件删除
     * @param groupName
     * @param remoteFileName
     * @throws Exception
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception{
        //获取TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        //删除文件
        storageClient.delete_file(groupName, remoteFileName);
    }

    /**
     * 获取Storage信息
     * @return StorageServer
     * @throws Exception
     */
    public static StorageServer getStorages() throws Exception{
        //创建一个Tracker访问的客户端对象TraClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TraClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取Storage信息
        return trackerClient.getStoreStorage(trackerServer);
    }

    /**
     * 获取Storage的IP和端口信息
     * @param groupName
     * @param remoteFileName
     * @return ServerInfo[]
     * @throws Exception
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception{
        //创建一个Tracker访问的客户端对象TraClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TraClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerServer的链接信息可以获取Storage的链接信息，创建StorageClient对象存储Storage的链接信息
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //获取Storage的IP和端口
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

    /**
     * 获取Tracker信息
     * @return String: url
     * @throws Exception
     */
    public static String getTrackerInfo() throws Exception{
        //获取TrackerServer
        TrackerServer trackerServer = getTrackerServer();

        String ip = trackerServer.getInetSocketAddress().getHostString();
        int tracker_http_port = ClientGlobal.getG_tracker_http_port();
        String url = "http://" + ip + ":" + tracker_http_port;
        return url;
    }

    /**
     * 获取TrackerServer
     * @return
     * @throws Exception
     */
    public static TrackerServer getTrackerServer() throws Exception {
        //创建一个Tracker访问的客户端对象TraClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TraClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    /**
     * 获取StorageClient
     * @param trackerServer
     * @return
     */
    public static StorageClient getStorageClient(TrackerServer trackerServer){
        //通过TrackerServer的链接信息可以获取Storage的链接信息，创建StorageClient对象存储Storage的链接信息
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }
}
