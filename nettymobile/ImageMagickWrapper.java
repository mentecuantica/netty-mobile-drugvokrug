/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;


/**
 * OOP Обертка над консольными утилитами ImageMagick
 *
 * Пока только под платформу WIN32
 *
 * @author Yuri Efimov
 * @version 0.1
 */
public class ImageMagickWrapper {

    public static final Logger LOG = Logger.getLogger(ImageMagickWrapper.class);
    String imgSource;

    String imgDest;
    int iWidth;
    int iHeight;

    static String libPath = "D:\\ImageMagick-6.3.9-Q16\\";

    public ImageMagickWrapper(String imageSource, String imageDest) {
        this.imgSource = imageSource;
        this.imgDest   = imageDest;
        //this.getSize();
    }

    public String getImgDest() {
        return imgDest;
    }

    public void setImgDest(String imgDest) {
        this.imgDest = imgDest;
    }

    /**
     * Получить источник изображения
     * @return
     */
    public String getImgSource() {
        return imgSource;
    }

    /**
     * Установить истлчник изображения
     * @param imgSource
     */
    public void setImgSource(String imgSource) {
        this.imgSource = imgSource;
    }

    /**
     * Получить размеры изображания
     */
    private void getSize() {
        String cmd = getBinary("identify");
        cmd+= " -format \"%w,%h\" " + this.getImgSource();
       
	    String result = this.execute(cmd);
        if (result==null) return;
        String[] sizes = result.split(",");
        this.iWidth  = Integer.parseInt(sizes[0]);
        this.iHeight = Integer.parseInt(sizes[1]);

    }

    /**
     * Выполнить команду
     * WIN32
     * @param cmd
     * @return
     */
    private String execute(String cmd)  {

       LOG.info("IMAGICK cmd: " +cmd);
       Process process = null;
       String line;
       try {
            process = Runtime.getRuntime().exec(cmd);
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            line = br.readLine();
       }
       catch (IOException ex) {
            return null;
       }
       return line;
    }

    /**
     * Сформировать тумбу любых размеров,
     * сохранится в по пути imgDest
     * 
     * @param width  - Ширина тумбы
     * @param height - Высота тумбы
     */
    public void generateThumb(int width, int height) {
        int maxWidth  = width*3;
    	int maxHeight = height*3;

    	String resize_cmd = " -resize ";
    	String cmd;

    	cmd = this.getBinary("convert");
    	cmd+= " -size " + maxWidth + "x"+maxHeight+" " + this.getImgSource();

    	cmd+= resize_cmd + width + "x" + height;
    	cmd+= "^^"; // special resize flag, since 6.3.8 version

        /*if ($this->platform == self::PLATFORM_WIN32) {
    		$cmd.='^'; // we have to escape ^ char for WIN32
    	} */

    	cmd+= " -gravity center -extent " + width + "x" + height;
    	cmd+= " " + this.getImgDest();
        this.execute(cmd);
    }

    /**
     * Получить полный путь к файлу
     * пока только WIN32
     * @param binaryFile
     * @return
     */
    public String getBinary(String binaryFile) {
        return ImageMagickWrapper.libPath + binaryFile + ".exe ";
    }

    /**
     * Установить путь к библиотеке
     * @param lp
     */
    public static void setLibPath(String lp) {
        ImageMagickWrapper.libPath = lp;
    }
    
  
}
