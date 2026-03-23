package org.fu.blockchain_backend.constants;

import java.lang.Exception;
import java.lang.RuntimeException;
import java.lang.String;

public class ContractConstants {
  public static String GreenCertAbi;

  public static String GreenCertBinary;

  public static String GreenCertGmBinary;

  static {
    try {
      GreenCertAbi = org.apache.commons.io.IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("abi/GreenCert.abi"));
      GreenCertBinary = org.apache.commons.io.IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("bin/ecc/GreenCert.bin"));
      GreenCertGmBinary = org.apache.commons.io.IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("bin/sm/GreenCert.bin"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
