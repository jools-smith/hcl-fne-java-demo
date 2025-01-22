package com.revenera.gcs.hcl;

import com.flexnet.licensing.client.ICapabilityResponseData;
import com.flexnet.licensing.client.IFeature;
import com.flexnet.licensing.client.ILicense;
import com.flexnet.licensing.client.IResponseStatus;
import com.flexnet.lm.FlxException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IO {

  public static void print(final String str) {
    System.out.println(str);
  }
  public static void lf() {
    System.out.println();
  }

  static void header(final String header) {
    print(header);
    print(header.chars().mapToObj(i -> "-").collect(Collectors.joining()));
  }

  static void header(final String header, final List<?>list) {
    header(header);
    print("size = " + list.size());
  }

  static String concatenate(final Object...parts) {
    return Arrays.stream(parts).map(Object::toString).collect(Collectors.joining(" | "));
  }

  static void printFeatureCollection(final String caption, final List<IFeature> features) {

    header(caption,features);

    for (final IFeature feature : features) {
      print(concatenate(feature.getName(),
                                     feature.getVersion(),
                                     feature.getCount(),
                                     feature.getStartDate(),
                                     feature.getExpiration(),
                                     feature.getAcquisitionStatus(),
                                     feature.getAvailableAcquisitionCount()));
    }
    lf();
  }

  static void printLicenseCollection(final String caption, final List<ILicense> licenses) {

    header(caption, licenses);

    for (final ILicense license : licenses) {
      print(concatenate(license.getName(),
                        license.getVersion(),
                        license.getCount(),
                        license.getStartDate(),
                        license.getExpiration()));
    }
    lf();
  }

  static void printResponseDetails(final ICapabilityResponseData response) throws FlxException {
    final List<IFeature> features = response.getFeatures();

    header("Capability Response Details", features);
    for (final IFeature feature : features) {
      print(concatenate(feature.getName(),
                        feature.getVersion(),
                        feature.getCount(),
                        feature.getStartDate(),
                        feature.getExpiration()));
    }
    lf();

    final List<IResponseStatus> statuses = response.getResponseStatus();

    header("Capability Response Status", statuses);
    for (final IResponseStatus status : statuses) {
      print(concatenate(status.getCode(), status.getStatus(), status.getCategory(), status.getDetails()));
    }
    lf();
  }
}
