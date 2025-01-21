package com.revenera.gcs.hcl;

import com.flexnet.licensing.client.ICapabilityResponseData;
import com.flexnet.licensing.client.IFeature;
import com.flexnet.licensing.client.ILicense;
import com.flexnet.licensing.client.IResponseStatus;
import com.flexnet.lm.FlxException;
import com.revenera.gcs.hcl.fne.Client;
import com.revenera.gcs.hcl.fne.ClientFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class IO {
  static void header(final String header) {
    System.out.println(header);
    System.out.println(header.chars().mapToObj(i -> "-").collect(Collectors.joining()));
  }

  static String concatenate(final Object...parts) {
    return Arrays.stream(parts).map(Object::toString).collect(Collectors.joining(" | "));
  }

  static void printFeatureCorrection(final String caption, final List<IFeature> features) {

    header(caption);

    System.out.println("count = " + features.size());
    for (final IFeature feature : features) {
      System.out.println(concatenate(feature.getName(),
                                     feature.getVersion(),
                                     feature.getCount(),
                                     feature.getStartDate(),
                                     feature.getExpiration(),
                                     feature.getAcquisitionStatus(),
                                     feature.getAvailableAcquisitionCount()));
    }
    System.out.println();
  }

  static void printLicenseCollection(final String caption, final List<ILicense> licenses) {

    header(caption);
    System.out.println("count = " + licenses.size());
    for (final ILicense license : licenses) {
      System.out.println(concatenate(license.getName(),
                                     license.getVersion(),
                                     license.getCount(),
                                     license.getStartDate(),
                                     license.getExpiration()));
    }
    System.out.println();
  }

  static void printResponseDetails(final ICapabilityResponseData response) throws FlxException {

    System.out.println("Capability Response Details");
    System.out.println("---------------------------");

    final List<IFeature> features = response.getFeatures();
    System.out.println("feature count = " + features.size());
    for (final IFeature feature : features) {
      System.out.println(concatenate(feature.getName(),
                                     feature.getVersion(),
                                     feature.getCount(),
                                     feature.getStartDate(),
                                     feature.getExpiration()));
    }

    final List<IResponseStatus> statuses = response.getResponseStatus();
    System.out.println("response status count = " + statuses.size());
    for (final IResponseStatus status : statuses) {
      status.getDetails();
      System.out.println(concatenate(status.getCode(), status.getStatus(), status.getCategory(), status.getDetails()));
    }
    System.out.println();
  }
}
public class Main {



  public static void main(final String...args) {
    try {
      System.out.println("hello...");

      final URI url = Client.deviceUri("flex1113-uat", "com");

      final Path storage = Paths.get("d:", "gcs", "hcl-fne-java-demo", "storage");

      final Client client = ClientFactory
              .createFactory()
              .withIdentity(IdentityClient.IDENTITY_DATA)
              .withStoragePath(storage)
              .withHostId("D16FE8E6E9D54E13B784D29788123B57")
              .withHostName("HCL Test Device")
              .withHostType("FLX_CLIENT")
              .initializeClient();

      IO.printFeatureCorrection("Features in Trusted Storage", client.manager().getFeaturesFromTrustedStorage(false));

      final ICapabilityResponseData response = client.callHome(url, "6ba6-6083-276d-4406-9296-981f-1cd0-e888");

      IO.printResponseDetails(response);

      final List<IFeature> features = client.manager().getFeaturesFromTrustedStorage(false);
      IO.printFeatureCorrection("Features in Trusted Storage", features);

      // acquire all features
      for (final IFeature feature : features) {
        client.acquire(feature.getName(), feature.getVersion(), 1);
      }
      IO.printLicenseCollection("Acquired Licenses", client.manager().getLicenses());

      // return all licenses
      for (final IFeature feature : features) {
        client.release(feature.getName());
      }
      IO.printLicenseCollection("Acquired Licenses", client.manager().getLicenses());
    }
    catch (final Throwable t) {
      t.printStackTrace(System.err);
    }
    finally {
      System.out.println("cheerio...");
    }
  }
}
