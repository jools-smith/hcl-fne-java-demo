package com.revenera.gcs.hcl;

import com.revenera.gcs.hcl.fne.Client;
import com.revenera.gcs.hcl.fne.ClientFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  public static void main(final String...args) {
    try {
      System.out.println("hello...");

      final String url = "https://flex1113-uat.compliance.flexnetoperations.com/deviceservices";

      final Path storage = Paths.get("d:", "gcs", "hcl-fne-java-demo", "storage");
      final Client client = ClientFactory
              .createFactory()
              .withIdentity(IdentityClient.IDENTITY_DATA)
              .withStoragePath(storage)
              .withHostId("D16FE8E6E9D54E13B784D29788123B57")
              .withHostName("HCL Test Device")
              .withHostType("FLX_CLIENT")
              .initializeClient();

      client.callHome(url, "6ba6-6083-276d-4406-9296-981f-1cd0-e888");

      client.manager().getFeaturesFromTrustedStorage(true).forEach(feature -> {
        System.out.println(feature.getName() + " | " + feature.getVersion()+ " | " + feature.getCount() +  " | " + feature.getStartDate() + " | " + feature.getExpiration());
      });
    }
    catch (final Throwable t) {
      t.printStackTrace(System.err);
    }
    finally {
      System.out.println("cheerio...");
    }
  }
}
