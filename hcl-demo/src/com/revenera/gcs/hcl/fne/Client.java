package com.revenera.gcs.hcl.fne;

import com.flexnet.licensing.client.*;
import com.flexnet.lm.FlxException;
import com.flexnet.lm.net.Comm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Client {
  final ILicensing licensing;


  /**
   * @return Activation URL in FNO
   */
  public static URI deviceUri(final String tenant, final String domain) {
    return URI.create(String.format("https://%s.compliance.flexnetoperations.%s/deviceservices", tenant, domain));
  }

  /**
   * @return Cloud License Server (CLS) URL in FNO
   */
  public static URI serverUri(final String tenant, final String domain, final String instanceId) {
    return URI.create(String.format("https://%s.compliance.flexnetoperations.%s/instances/%s/request", tenant, domain, instanceId));
  }

  Client(final ILicensing licensing) {
    this.licensing = licensing;
  }


  /**
   * @return License Manager interface
   */
  public ILicenseManager manager() {
    return this.licensing.getLicenseManager();
  }

  /**
   * acquire (check-out) specific feature
   * @param feature
   * @param version
   * @param count
   * @return
   * @throws FlxException
   */
  public ILicense acquire(final String feature, final String version, final long count) throws FlxException {
    return manager().acquire(feature, version, count);
  }

  /**
   * return (check-in) specific license
   * @param license
   * @throws FlxException
   */
  public void returnLicense(final ILicense license) throws FlxException {
    manager().releaseLicense(license);
  }

  /**
   * return (check-in) license by feature name
   * @param feature
   * @throws FlxException
   */
  public void returnLicense(final String feature) throws FlxException {

    for (final ILicense license : manager().getLicenses(feature)){
      returnLicense(license);
    }
  }

  /**
   * get all acquired licenses
   * @return
   */
  public List<ILicense> getLicenses() {
    return manager().getLicenses();
  }

  public byte[] activationData(final String...rights) throws FlxException {

    final ICapabilityRequestOptions options = manager().createCapabilityRequestOptions();

    options.forceResponse();

    for(final String activationId : rights) {
      options.addRightsId(activationId, 1);
    }

    // request data for FNO
    return manager().generateCapabilityRequest(options);
  }

  /**
   *
   * @param uri - FNO activation URL
   * @param rights - zero or more activation IDs to activate
   * @return capability response object
   * @throws FlxException
   */
  public ICapabilityResponseData activate(final URI uri, final String...rights) throws FlxException {

    // request data for FNO
    final byte[] request = activationData(rights);

    // FNO response
    final byte[] response = Comm.getHttpInstance(uri.toString()).sendBinaryMessage(request);

    return manager().processCapabilityResponse(response);
  }

  /**
   * Refresh license from FNO device - will return all features mapped to the device in FNP
   * @param uri - FNO activation URL
   * @return
   * @throws FlxException
   */
  public ICapabilityResponseData callhome(final URI uri) throws FlxException {
    return activate(uri);
  }
  /**
   *
   * @param responseFile - path to response file
   * @return capability response object
   * @throws FlxException
   * @throws IOException
   */
  public ICapabilityResponseData activateOffline(final Path responseFile) throws FlxException, IOException {

    try (final InputStream stream = Files.newInputStream(responseFile)) {
      return manager().processCapabilityResponse(stream);
    }
  }

}
