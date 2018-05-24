package cn.hzw.graffiti.bean;

/**
 * Created by lj_xwl on 2018/5/23.
 */

public class imageMessage {
    public String imageName;
    public String imageBase64;

    public imageMessage(String imageName, String imageBase64) {
        this.imageName = imageName;
        this.imageBase64 = imageBase64;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    @Override
    public String toString() {
        return "imageMessage{" +
                "imageName='" + imageName + '\'' +
                ", imageBase64='" + imageBase64 + '\'' +
                '}';
    }
}
