package com.revenera.gcs.hcl;

import com.flexnet.licensing.client.ICapabilityResponseData;
import com.flexnet.licensing.client.IFeature;
import com.flexnet.lm.FlxException;
import com.revenera.gcs.hcl.fne.Client;
import com.revenera.gcs.hcl.fne.ClientFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

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

      IO.printFeatureCollection("Features in Trusted Storage", client.manager().getFeaturesFromTrustedStorage(false));

      // activate license
      final ICapabilityResponseData response = client.callHome(url, "6ba6-6083-276d-4406-9296-981f-1cd0-e888");

      IO.printResponseDetails(response);

      // get features in TS
      final List<IFeature> features = client.manager().getFeaturesFromTrustedStorage(false);
      IO.printFeatureCollection("Features in Trusted Storage", features);

      // acquire all features
      for (final IFeature feature : features) {
        client.acquire(feature.getName(), feature.getVersion(), 1);
      }
      IO.printLicenseCollection("Acquired Licenses", client.manager().getLicenses());

      // return all licenses -- the hard way
      for (final IFeature feature : features) {
        client.returnLicense(feature.getName());
      }

//      for (final ILicense license : client.manager().getLicenses()) {
//        client.manager().returnLicense(license);
//      }


//       client.manager().returnAllLicenses();

      IO.printLicenseCollection("Acquired Licenses", client.manager().getLicenses());

      client.acquire("dummy", "0", 1);
    }
    catch (final FlxException e) {
      IO.header("Licensing Exception");
      IO.print(IO.concatenate(e.getClass().getSimpleName(), e.getMessage(), e.getDiagnosticMessage()));

      Optional.ofNullable(e.getArguments()).ifPresent(value -> {
        IO.print(IO.concatenate(value));
      });

    }
    catch (final Throwable t) {
      IO.header("Unexpected Exception");
      IO.header(IO.concatenate(t.getClass().getName(), t.getMessage()));
    }
    finally {
      System.out.println("cheerio...");
    }
  }
}
