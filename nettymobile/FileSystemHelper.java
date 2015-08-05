/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

import java.io.File;

/**
 *
 * @author с
 */
public class FileSystemHelper {

    /**
     * Creates a directory path from UIN number
     * @example
     * int UIN = 92211
     * createUinPath(UIN, "profiles//") return
     * String "profiles//9//2//2//1//1//" and creates
     * corresponding directories
     *
     * @param uin
     * @param pathPrefix
     * @return
     */
    public static String createUinPath(int uin, String pathPrefix) {
        String directory = pathPrefix;
        String strId = String.valueOf(uin);
        for (int i=0; i<strId.length(); i++) {
            directory = directory + strId.charAt(i) + "//";
            System.out.println(directory);
            File temp = new File(directory);
            if (!temp.exists()) {
                temp.mkdir();
            }
        }
        return directory;
    }

    public static String getUinPath(int uin, String pathPrefix) {
        String directory = pathPrefix;
        String strId = String.valueOf(uin);
        for (int i=0; i<strId.length(); i++) {
            directory = directory + strId.charAt(i) + "//";
        }
        return directory;
    }
}
