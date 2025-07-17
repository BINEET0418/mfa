package com.example.tool.Controller;

import com.example.tool.dto.GenerateQrRequestDTO;
import com.example.tool.service.OtpService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    @Value("${aes.key}")
    private String AES_KEY;

    @Value("${hmac.key}")
    private String HMAC_KEY;

    private final OtpService otpService;

    public QrController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] generateQr(@RequestBody GenerateQrRequestDTO request) throws Exception {
        String userId = request.getUserId();

        // 1. Generate TOTP secret
        String secret = otpService.setupOtpForUser(userId);
        System.out.println("secret = " + secret + "   username =  " + userId);

        // 2. Create payload
        String payload = userId + "|" + secret;
        System.out.println("payload = " + payload);

        String mac = hmac(payload, HMAC_KEY);
        System.out.println("mac = " + mac);

        String finalPayload = payload + "|" + mac;
        System.out.println("finalPayload = " + finalPayload);

        // 3. Encrypt payload with AES-ECB
        String encrypted = encrypt(finalPayload, AES_KEY);
        System.out.println("encrypted = " + encrypted);

        // 4. Encode as QR
        BitMatrix matrix = new MultiFormatWriter().encode(encrypted, BarcodeFormat.QR_CODE, 300, 300);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        return baos.toByteArray();
    }

    private String encrypt(String data, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String hmac(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}
