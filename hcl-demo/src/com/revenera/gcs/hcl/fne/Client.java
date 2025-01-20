package com.revenera.gcs.hcl.fne;

import com.flexnet.licensing.client.ICapabilityRequestOptions;
import com.flexnet.licensing.client.ICapabilityResponseData;
import com.flexnet.licensing.client.ILicenseManager;
import com.flexnet.licensing.client.ILicensing;
import com.flexnet.lm.FlxException;
import com.flexnet.lm.net.Comm;

import java.util.Arrays;

public class Client {
  final ILicensing licensing;


  Client(final ILicensing licensing) {
    this.licensing = licensing;
  }

  public ILicenseManager manager() {
    return this.licensing.getLicenseManager();
  }

  public void callHome(final String uri, final String...rights) throws FlxException {

    final ILicenseManager manager = manager();

    final ICapabilityRequestOptions options = manager.createCapabilityRequestOptions();

    options.forceResponse();

    Arrays.stream(rights).forEach(right -> {
      options.addRightsId(right, 1);
    });

    final byte[] request = manager.generateCapabilityRequest(options);

    final byte[] response = Comm.getHttpInstance(uri).sendBinaryMessage(request);

    final ICapabilityResponseData capabilityResponse = manager.processCapabilityResponse(response);

  }

}
