package com.kingkingdu.compressor

import com.kingkingdu.CompressInfo
import com.kingkingdu.ImgCompressExtension
import com.kingkingdu.util.FileUtils
import com.kingkingdu.util.Logger
import com.kingkingdu.util.PngquantUtil
import com.kingkingdu.util.ZopflipngUtil
import org.gradle.api.Project

public class ZopflipngCompressor implements ICompressor{
    int keyIndex = 0
    def project;
    def compressInfoList = new ArrayList<CompressInfo>()
    ImgCompressExtension config
    def beforeTotalSize = 0
    def afterTotalSize = 0
    Logger log

    @Override
    void compress(Project rootProject,List<CompressInfo> unCompressFileList,ImgCompressExtension config) {
        this.project = rootProject
        this.compressInfoList = compressInfoList
        this.config = config
        log = Logger.getInstance(rootProject)
        ZopflipngUtil.copyZopflipng2BuildFolder(project)
        log.i("type>>ZopflipngCompressor")
        PngquantUtil.copyPngquant2BuildFolder(project)
        def zopflipng = ZopflipngUtil.getZopflipngFilePath(project)
        unCompressFileList.each { info ->
            File originFile = new File(info.path)
            String type = originFile.getAbsolutePath().substring(originFile.getAbsolutePath().indexOf("."))
            String suffix = ""
            log.i("type>>" + type)
            if (type.equals(".png")){
                suffix = ".png"
            }

            long originalSize = originFile.length()
            Process process = new ProcessBuilder(zopflipng, "-y", "-m",info.path,info.outputPath).redirectErrorStream(true).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))
            StringBuilder error = new StringBuilder()
            String line
            while (null != (line = br.readLine())) {
                error.append(line)
            }
            int exitCode = process.waitFor()

            if (exitCode == 0) {
                long optimizedSize = new File(info.outputPath).length()
                float rate = 1.0f * (originalSize - optimizedSize) / originalSize * 100
                log.i("Succeed! ${originalSize}B-->${optimizedSize}B, ${rate}% saved! ${info.outputPath}")
                beforeTotalSize += originalSize
                afterTotalSize += optimizedSize
            } else if (exitCode == 98) {
                log.w("Skipped! ${info.outputPath}")
            } else {
                log.e("Failed! ${info.outputPath}")
            }
        }

        log.i("Task finish, compress ${unCompressFileList.size()} files, before total size: ${FileUtils.formetFileSize(beforeTotalSize)} after total size: ${FileUtils.formetFileSize(afterTotalSize)}")
    }






}