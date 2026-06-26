package com.unemployedteam.saferoom.fieldreport;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Component
public class QrCodeGenerator {

  public String generateBase64QrCode(String content, int width, int height) {
    try {
      QRCodeWriter writer = new QRCodeWriter();
      BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(matrix, "PNG", bos);
      return Base64.getEncoder().encodeToString(bos.toByteArray());
    } catch (WriterException | IOException e) {
      log.error("QR 코드 생성 실패: {}", e.getMessage());
      throw new RuntimeException("QR 코드 생성 실패", e);
    }
  }
}