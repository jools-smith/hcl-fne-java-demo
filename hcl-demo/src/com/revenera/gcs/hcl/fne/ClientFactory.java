package com.revenera.gcs.hcl.fne;

import com.flexnet.licensing.client.ILicenseManager;
import com.flexnet.licensing.client.ILicensing;
import com.flexnet.licensing.client.LicensingFactory;
import com.flexnet.lm.FlxException;
import com.flexnet.lm.SharedConstants;

import java.nio.file.Path;

public class ClientFactory {
  byte[] identity;
  String storagePath;
  String hostId;
  String hostName;
  String hostType;
  SharedConstants.HostIdType hostIdType;

  public static ClientFactory createFactory() throws FlxException {
    return new ClientFactory();
  }

  public Client initializeClient() throws FlxException {
    final ILicensing licensing = LicensingFactory.getLicensing(this.identity, this.storagePath);

    final ILicenseManager mgr = licensing.getLicenseManager();

    mgr.setHostName(this.hostName);
    mgr.setHostType(this.hostType);
    if (this.hostIdType != null) {
      mgr.setHostId(this.hostIdType, this.hostId);
    }
    else {
      licensing.setCustomHostID(this.hostId);
    }

    if (this.storagePath != null) {
      mgr.addTrustedStorageLicenseSource();
    }

    return new Client(licensing);
  }

  public ClientFactory withIdentity(final byte[] value) {
    this.identity = value;
    return this;
  }

  public ClientFactory withStoragePath(final Path value) {
    return withStoragePath(value.toAbsolutePath().toString());
  }

  public ClientFactory withStoragePath(final String value) {
    this.storagePath = value;
    return this;
  }

  public ClientFactory withHostId(final String value) {
    this.hostId = value;
    return this;
  }

  public ClientFactory withHostName(final String value) {
    this.hostName = value;
    return this;
  }

  public ClientFactory withHostType(final String value) {
    this.hostType = value;
    return this;
  }

  public ClientFactory withHostId(final SharedConstants.HostIdType value, final String id) {
    this.hostIdType = value;
    this.hostId = id;
    return this;
  }
}
