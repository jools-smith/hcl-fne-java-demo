package com.revenera.gcs.hcl.fne;

import com.flexnet.licensing.client.ILicenseManager;
import com.flexnet.licensing.client.ILicensing;
import com.flexnet.licensing.client.LicensingFactory;
import com.flexnet.lm.FlxException;
import com.flexnet.lm.SharedConstants;

import java.nio.file.Path;

public class ClientFactory {
  // crypto keys downloaded for FNO
  byte[] identity;
  // trusted file path - may be null in which case there is no persistence and license must be loaded on each program start
  String storagePath;
  // unique id for this device
  String hostId;
  // friendly name for this device: does not need to be unique
  String hostName;
  // device type which must be defined in FNO: default FLX_CLIENT
  String hostType;
  // type of locally recognized hostid - null => using simple string hostid
  SharedConstants.HostIdType hostIdType;

  public static ClientFactory createFactory() throws FlxException {
    return new ClientFactory();
  }

  public ClientFactory withIdentity(final byte[] value) {
    this.identity = value;
    return this;
  }

  public ClientFactory withStoragePath(final Path value) {
    this.storagePath = value.toAbsolutePath().toString();
    return this;
  }

  public ClientFactory withHostId(final String value) {
    this.hostId = value;
    return this;
  }

  public ClientFactory withHostId(final SharedConstants.HostIdType value, final String id) {
    this.hostIdType = value;
    this.hostId = id;
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

  public Client initializeClient() throws FlxException {
    // if storage path is null, will operate using in-memory trusted storage with no persistence
    final ILicensing licensing = LicensingFactory.getLicensing(this.identity, this.storagePath);

    final ILicenseManager mgr = licensing.getLicenseManager();

    if (this.hostName != null) {
      mgr.setHostName(this.hostName);
    }

    if (this.hostType != null) {
      mgr.setHostType(this.hostType);
    }

    if (this.hostIdType != null) {
      // add specific host it - this must be recognized by the toolkit e.g. MAC address
      mgr.setHostId(this.hostIdType, this.hostId);
    }
    else {
      // add simple string hostid
      licensing.setCustomHostID(this.hostId);
    }

    mgr.addTrustedStorageLicenseSource();

    return new Client(licensing);
  }
}
