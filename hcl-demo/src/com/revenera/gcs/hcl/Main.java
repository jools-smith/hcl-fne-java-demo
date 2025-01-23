package com.revenera.gcs.hcl;

import com.flexnet.licensing.client.ICapabilityResponseData;
import com.flexnet.licensing.client.IFeature;
import com.flexnet.licensing.client.ILicense;
import com.flexnet.lm.FlxException;
import com.revenera.gcs.hcl.fne.Client;
import com.revenera.gcs.hcl.fne.ClientFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Main {

  static final URI url = Client.deviceUri("flex1113-uat", "com");

  static void showResponseDetails(final ICapabilityResponseData response) throws FlxException {
    IO.printResponseDetails(response);
  }

  static void showFeaturesInTrustedStorage(final Client client) throws FlxException {
    final List<IFeature> features = client.manager().getFeaturesFromTrustedStorage(false);
    IO.printFeatureCollection("Features in Trusted Storage", features);
  }

  static void acquireLicensesForAllFeatures(final Client client) throws FlxException {
    final List<IFeature> features = client.manager().getFeaturesFromTrustedStorage(false);

    // acquire all features
    for (final IFeature feature : features) {
      final ILicense license = client.acquire(feature.getName(), feature.getVersion(), feature.getCount());
      IO.print(IO.concatenate("acquired license",
              license.getName(),
              license.getVersion(),
              license.getCount(),
              license.getStartDate(),
              license.getExpiration()));
    }
  }

  static void showAcquiredFeatures(final Client client) throws FlxException {
    IO.printLicenseCollection("Acquired Licenses", client.manager().getLicenses());
  }

  static void returnAllFeatures(final Client client) throws FlxException {
    final List<IFeature> features = client.manager().getFeaturesFromTrustedStorage(false);

    // return all licenses -- the hard way
    for (final IFeature feature : features) {
      client.returnLicense(feature.getName());
    }
  }

  static void testOnline(final Path root) throws FlxException {
    final Path storage = Paths.get(root.toString(), "storage");

    final Client client = ClientFactory
            .createFactory()
            .withIdentity(IdentityClient.IDENTITY_DATA)
            .withStoragePath(storage)
            .withHostId("D16FE8E6E9D54E13B784D29788123B57")
            .withHostName("HCL Test Device")
            .withHostType("FLX_CLIENT")
            .initializeClient();

    // there will be nothing in TS initially but will be populated on subsequent restarts
    showFeaturesInTrustedStorage(client);

    // activate directly from FNO - this will created the device if it doesn't exist
    final ICapabilityResponseData response = client.activate(url, "f577-eda5-3060-4026-85d8-7d4c-3913-a75e");
    showResponseDetails(response);

    showFeaturesInTrustedStorage(client);

    showAcquiredFeatures(client);

    acquireLicensesForAllFeatures(client);
    showAcquiredFeatures(client);

    returnAllFeatures(client);
    showAcquiredFeatures(client);

    // refresh frn FNO
    client.callhome(url);
  }

  static void testOfflineInMemory(final Path root) throws FlxException, IOException {
    final Path responseFile = Paths.get(root.toString(), "OFFLINE-DEVICE-0001.bin");
    final Client client = ClientFactory
            .createFactory()
            .withIdentity(IdentityClient.IDENTITY_DATA)
            .withHostId("OFFLINE-DEVICE-0001")
            .withHostType("FLX_CLIENT")
            .initializeClient();

    // there will be nothing in TS as we are using in-memory TS
    showFeaturesInTrustedStorage(client);

    // process response file downloaded form FNO
    final ICapabilityResponseData response = client.activateOffline(responseFile);
    showResponseDetails(response);

    showFeaturesInTrustedStorage(client);

    showAcquiredFeatures(client);

    acquireLicensesForAllFeatures(client);
    showAcquiredFeatures(client);

    returnAllFeatures(client);
    showAcquiredFeatures(client);

    // refresh frn FNO
    client.callhome(url);
  }

  public static void main(final String...args) {
    try {
      final Path root = Paths.get("d:", "gcs", "hcl-fne-java-demo");

      IO.header("ON-LINE TEST");
      testOnline(root);

      IO.header("OFF-LINE IN-MEMORY TEST");
      testOfflineInMemory(root);

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
