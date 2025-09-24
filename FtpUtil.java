import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Mao.Entry;
import jline.internal.InputStreamReader;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReplay;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import com.alibaba.fastjson.util.IOUtils;
import com.sun.star.uno.RuntimeException;


public class FtpUtils{
    private static final Logger log = Logger.getLogger(FtpUtils.class);

    public static FTPClient getFtpClient(String url, int port, String username, String password){
        FTPClient ftpClient = null;
        int reply;

        ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(30000);
        try{
            ftpClient.connect(url, port);
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            if(!FTPReplay.isPostitiveCompletion(reply)){
                ftpClient.disconnect();
                log.error("ftp连接失败！连接地址-------->" + url);
                return null;
            }

            if(FTPReply.isPostiveCompletion(ftpClient.sendCommand(
                "OPTS UTF8", "ON"
            ))){
                ftpClient.setControlEncoding("UTF-8");
            }else{
                ftpClient.setControlEncoding("UTF-8");
            }

        }catch(Exception e){
            log.error("ftp连接出错！连接地址-------->" + url, e);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return ftpClient;
    }

    public static InputStream getFtpFileInputStream(FTPClient ftpClient, String path, String fileName){
        InputStream inputStream = null;

        if(ftpClient == null){
            return inputStream;
        }

        try{
            ftpClient.changeWorkingDirectory(path);
            FTPFile[] listFiles = ftpClient.listFiles();
            if(listFiles.length > 0){
                inputStream = ftpClient.retrieveFileStream(new String(fileName.getBytes("utf-8"), FTP.DEFAULT_CONTROL_ENCODING));
            }
        }catch(Exception e){
            log.error("获取ftp文件输入流出错！文件路径-------->" + path + File.separator + fileName, e);
            System.out.print(e.getMessage());
            e.printStackTrace();
        }finally{
            if(ftpClient != null){
                disConnection(ftpClient);
            }
        }

        return inputStream;
    }

    public static boolean deleteFtpFile(String url, int port, String username, String password, String fileName, String encode){
        FTPClient ftpClient = getFtpClient(url, port, username, password);
        if(ftpClient == null){
            log.error("ftp链接失败！地址为------->" + url + port);
        }

        boolean isFlag = false;

        List<String> fileName = new ArrayList();
        try{
            ftpClient.changeWorkingDirectory("/");
            ftpClient.dele(fileName);
            isFlag = true;
        }catch(Exception e){
            log.error("删除ftp文件出错！地址为-------->" + url + ":" + port + File.separator + fileName, e);
            System.out.print(e.getMessage());
            e.printStackTrace();
        }finally{
            if(ftpClient != null){
                disConnection(ftpClient);
            }
        }
        return isFlag;
    }

    public static List<String> getFtpAllFile(String url, int port, String username, String password, String path, String encode){
        int i = 0;
        FTPClient ftpClient = getFtpClient(url, port, username, password);
        if(ftpClient == null){
            log.error("ftp链接失败！地址为---->" + url + port);
        }
        BufferedReader br = null;
        InputStream in = null;
        return getFtpFAllFileName(ftpClient, path);
    }

    private static List<String> getFtpFAllFileName(FTPClient ftpClient, String path){
        List<String> fileNames = new ArrayList();
        try{
            FTPFile[] listFiles = ftpClient.listFiles();
            if(listFiles.length > 0){
                for(int i = 0;  i < listFiles.length; i ++){
                    if(listFiles[i].isFile()){
                        String fileName = new String(listFiles[i].getName().getBytes("gbk"), "utf-8"); //win下默认为gbk
                        fileNames.add(fileName);
                    }
                }
            }
        }catch(Exception e){
            log.error("获取ftp所有文件名称出错！地址为-------->" + path, e);
            System.out.print(e.getMessage());
            e.printStackTrace();
        }finally{
            if(ftpClient != null){
                disConnection(ftpClient);
            }
        }
        return fileNames;
    }

    public static String getFtpFile(String url, int port, String username,
                                    String password, String path, String fileName, String encode){
        
        FTPClient ftpClient = getFtpClient(url, port, username, password);
        if(ftpClient == null){
            log.error("ftp链接失败！地址为---->" + url + port);
        }

        BufferedReader br = null;
        InputStream in = null;
        in = getFtpFileInputStream(ftpClient, path, fileName);

        StreamBuffer result = new StringBuffer();
        if(in == null){
            log.error("读取文件失败！");
            return null;
        }

        if(encode == null){
            encode = "UTF-8";
        }

        try{
            br = new BufferedReader(new InputStreamReader(in, encode));
            String data = null;
            while((data = br.readLine()) != null){
                result.append(data + "\n");
            }
        }catch(Exception e){
            log.error("获取ftp文件出错！地址为-------->" + url + ":" + port + File.separator + path + File.separator
            + fileName, e);

            e.printStackTrace();
        }finally{
            disConnection(ftpClient);
            if(br != null){
                try{
                    br.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(in != null){
                try{
                    in.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        return result.toString();
    }

    public static void disConnection(FTPClient ftpClient){
        try{
            if(ftpClient.isConnected()){
                ftpClient.disconnect();
            }
        }catch(IOException e){
            log.error("关闭ftp服务器！", e);
            e.printStackTrace();
        }
    }

    public static InputStream createXML(HashMap HashMap){
        File file = null;
        InputStream inputStream = null;

        Document createDocument = DocumentHelper.createDocument();
        Element head = createDocument.addElement("head");

        Iterator<Map.Entry<String, Object>> iterator = hashMap.entrySet().iterator();

        while(iterator.hasNext()){
            Entry<String, Object> next = iterator.next();
            Element key = head.addElement(next.getKey());
            key.setText(next.getValue().toString());
        }

        OutputFormat format = OutputFormat.createCompactFormat();
        format.setEncoding("UTF-8");
        format.setTrimText(false);
        ByteArrayOutputStream outPut = new ByteArrayOutputStream();
        try{
            XMLWriter xmlWriter = new XMLWriter(outPut, format);
            xmlWriter.write(createDocument);
            xmlWriter.close();
            inputStream = new ByteArrayOutputStream(outPut.toByteArray());
        }catch(Exception e){
            log.error("创建xml文件出错！", e);
            e.printStackTrace();
        }
        return inputStream;
    }

    public static InputStream createXML(HashMap hashMap, List<HashMap> submMaps){
        File file = null;
        InputStream inputStream = null;
        Document createDocument = DocumentHelper.createDocument();
        Element head = createDocument.addElement("head");
        Element body = head.addElement("body");

        Iterator<Map.Entry<String, Object>> iterator = hashMap.entrySet().iterator();
        while(iterator.hasNext()){
            Entry<String, Object> next = iterator.next();
            Element key = head.addElement(next.getKey());
            key.setText(next.getValue().toString());
        }

        for(HashMap hashMaps : submMaps){
            iterator = hashMap.entrySet().iterator();
            Element addElement = body.addElement(next.getKey());
            while(iterator.hasNext())
            {
                Entry<String, Object> next = iterator.next();
                Element key = addElement.addElement(next.getKey());
                key.setText(next.getValue().toString());
            }
        }

        OutputFormat format = OutputFormat.createCompactFormat();
        format.setEncoding("UTF-8");
        format.setTrimText(false);
        ByteArrayOutputStream outPut = new ByteArrayOutputStream();
        try{
            XMLWriter xmlWriter = new XMLWriter(outPut, format);
            xmlWriter.write(createDocument);
            xmlWriter.close();
            inputStream = new ByteArrayOutputStream(outPut.toByteArray());
            return inputStream;
        }catch(Exception e){
            log.error("创建xml文件出错！", e);
            e.printStackTrace();
        }
        return inputStream;
    }

    //上传文件
    public static boolean upload(String url, Integer port, String userName, String pwd, 
        InputStream inputStream, String remotePath, String fileName){

            FTPClient ftpClient = new FTPClient();

            boolean flag;
            try{
                ftpClient.connect(url, port);
                ftpClient.login(userName, pwd);

                //创建多级目录
                makeDir(ftpClient, remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("GBK");
                //设置文件类型（二进制）
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode(); //被动模式
                ftpClient.storeFile(new String(fileName.getBytes("UTF-8"),"iso-8859-1"),
                inputStream);
                inputStream.close();
                ftpClient.logout();
                flag = true;
                System.out.println(fileName + "上传成功！");
            }catch(Exception e){
                log.error("上传ftp文件-------->" + url + ":" + port + File.separator + remotePath + File.separator + fileName, e);
                throw new RuntimeException("ftp客户端出错", e);
            }finally{
                IOUtils.close(inputStream);
                if(ftpClient.isConnected()){
                    try{
                        ftpClient.disconnect();
                    }catch(IOException e){
                        log.error("关闭ftp连接发生异常", e);
                    }
                }
            }
            return flag;
    }

    public static boolean makeDir(FTPClient ftp, String path)
        throws IOException{
            String[] paths = path.split("/");
            //创建成功标识
            boolean isMakeSuccess = false;
            for(String str : paths){
                //切换目录，根据切换是否成功判断子目录是否存在
                boolean changeSuccess = ftp.changeWorkingDirectory(str);
                //该级路径不存在就创建并切换
                if(!changeSuccess){
                    isMakeSuccess = ftp.makeDirectory(str);
                    ftp.changeWorkingDirectory(str);
                }
            }
            return isMakeSuccess;
    }

    //读取输入流长度
    public static int readLen(InputStream inputStream){
        int length = 0;
        try{
            length = inputStream.available();
        }catch(IOException e){
            log.error("读取输入流长度出错！", e);
        }
        return length;
    }
}