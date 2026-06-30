package com.aifc.system.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfUtil
{
    private PdfUtil()
    {
    }

    public static String pdfToString(String localPath) throws Exception
    {
        if (localPath == null || !Files.exists(Paths.get(localPath)))
        {
            return null;
        }
        try (InputStream inputStream = Files.newInputStream(Paths.get(localPath)))
        {
            return pdfToString(inputStream);
        }
    }

    public static String pdfToString(InputStream inputStream) throws Exception
    {
        if (inputStream == null)
        {
            return null;
        }
        try (PDDocument document = PDDocument.load(inputStream))
        {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    public static String pdfToStringByOcr(String localPath) throws Exception
    {
        if (localPath == null || !Files.exists(Paths.get(localPath)))
        {
            return null;
        }
        if (!isTesseractAvailable())
        {
            return null;
        }
        List<File> tempImages = new ArrayList<>();
        StringBuilder allText = new StringBuilder();
        try (PDDocument document = PDDocument.load(new File(localPath)))
        {
            PDFRenderer renderer = new PDFRenderer(document);
            int pages = document.getNumberOfPages();
            for (int i = 0; i < pages; i++)
            {
                BufferedImage image = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
                File tempImage = File.createTempFile("aifc_ocr_" + UUID.randomUUID(), ".png");
                ImageIO.write(image, "png", tempImage);
                tempImages.add(tempImage);
                String pageText = runTesseract(tempImage.getAbsolutePath());
                if (pageText != null)
                {
                    allText.append(pageText).append("\n");
                }
            }
        }
        finally
        {
            for (File file : tempImages)
            {
                if (file != null && file.exists())
                {
                    file.delete();
                }
            }
        }
        return allText.toString();
    }

    private static boolean isTesseractAvailable()
    {
        try
        {
            Process process = new ProcessBuilder("tesseract", "--version").start();
            int exit = process.waitFor();
            return exit == 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static String runTesseract(String imagePath) throws Exception
    {
        Process process = new ProcessBuilder(
            "tesseract",
            imagePath,
            "stdout",
            "-l",
            "chi_sim+eng").start();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = process.getInputStream().read(buffer)) != -1)
        {
            baos.write(buffer, 0, len);
        }
        process.waitFor();
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
