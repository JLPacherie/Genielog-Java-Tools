package com.genielog.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	public static boolean unzip(String fileZip, File destDir) throws IOException {
		byte[] buffer = new byte[1024];
		boolean result = (fileZip != null) && (destDir.canWrite());
		if (result) {
			try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
				ZipEntry zipEntry = zis.getNextEntry();
				while (zipEntry != null) {
					File newFile = newFile(destDir, zipEntry);
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
					zipEntry = zis.getNextEntry();
				}
				zis.closeEntry();
			} catch (IOException e) {
				result = false;
			}
		}
		return result;
	}

	public static void zipOneFile(String sourceFile, String zipFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		File fileToZip = new File(sourceFile);
		FileInputStream fis = new FileInputStream(fileToZip);

		ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		zipOut.close();
		fis.close();
		fos.close();
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
}
