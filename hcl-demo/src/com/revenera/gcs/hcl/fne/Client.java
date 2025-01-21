package com.revenera.gcs.hcl.fne;

import com.flexnet.licensing.client.*;
import com.flexnet.lm.FlxException;
import com.flexnet.lm.net.Comm;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
  final ILicensing licensing;


  public static URI deviceUri(final String tenant, final String domain) {
    return URI.create(String.format("https://%s.compliance.flexnetoperations.%s/deviceservices", tenant, domain));
  }

  public static URI serverUri(final String tenant, final String domain, final String instanceId) {
    return URI.create(String.format("https://%s.compliance.flexnetoperations.%s/instances/%s/request", tenant, domain, instanceId));
  }

  Client(final ILicensing licensing) {
    this.licensing = licensing;
  }


  public ILicenseManager manager() {
    return this.licensing.getLicenseManager();
  }

  public ILicense acquire(final String feature, final String version, final long count) throws FlxException {
    return manager().acquire(feature, version, count);
  }

  public void release(final ILicense license) throws FlxException {
    manager().releaseLicense(license);
  }

  public void release(final String feature) throws FlxException {
     for(final ILicense license : new ArrayList<>(manager().getLicenses(feature)))    {
       release(license);
     }
  }

  public List<ILicense> getLicenses() {
    return manager().getLicenses();
  }

  public ICapabilityResponseData callHome(final URI uri, final String...rights) throws FlxException {

    final ICapabilityRequestOptions options = manager().createCapabilityRequestOptions();

    options.forceResponse();

    Arrays.stream(rights).forEach(right -> {
      options.addRightsId(right, 1);
    });

    final byte[] request = manager().generateCapabilityRequest(options);

    final byte[] response = Comm.getHttpInstance(uri.toString()).sendBinaryMessage(request);

    return manager().processCapabilityResponse(response);
  }

}
