package com.cong;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeSecretKeyExtractor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(" QRCodeSecretExtractor qrCodeimage");
            System.exit(1);
        }

        String qrCodePath = args[0]; // 从命令行参数获取二维码图片路径

        try {
            String qrCodeData = decodeQRCode(new File(qrCodePath));
            if (qrCodeData == null) {
                System.out.println("未能解码二维码");
                return;
            }
            System.out.println("QR Code Data: " + qrCodeData);

            // 根据数据格式解析 secret 密钥
            String secretKey = extractSecretKey(qrCodeData);
            if (secretKey != null) {
                System.out.println("Extracted Secret Key: " + secretKey);
            } else {
                System.out.println("未能提取到 secret 密钥");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 解码二维码
    private static String decodeQRCode(File qrCodeimage) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(qrCodeimage);
        if (bufferedImage == null) {
            System.out.println("bufferedImage is null");
            return null;
        }
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

    // 提取 secret 密钥
    private static String extractSecretKey(String qrCodeData) throws URISyntaxException {
        if (qrCodeData.startsWith("otpauth://")) {
            // 解析 otpauth URI 格式
            return parseOTPAuthURI(qrCodeData).get("secret");
        } else if (qrCodeData.startsWith("https://") || qrCodeData.startsWith("http://")) {
            // 处理 URL 格式，可以根据需要实现 HTTP 请求获取 secret
            // 示例中假设 secret 密钥是直接返回的
            // 您可以调用之前提到的 fetchSecretKeyFromURL 方法
            System.out.println("二维码包含 URL，需要进一步处理");
            return null; // 实际实现中返回从 URL 获取的 secret
        } else {
            // 假设是纯文本格式
            return qrCodeData;
        }
    }

    // 解析 otpauth URI
    private static Map<String, String> parseOTPAuthURI(String uriString) throws URISyntaxException {
        URI uri = new URI(uriString);
        String query = uri.getQuery();
        String[] pairs = query.split("&");
        Map<String, String> queryParams = new HashMap<>();

        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0 && idx < pair.length() - 1) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                queryParams.put(key, value);
            }
        }

        return queryParams;
    }
}
