package org.folio.rs.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class LocationMappingFilterData {
  private final String finalLocationId;
  private final String remoteStorageId;
  private final String originalLocationId;
  private final int offset;
  @Builder.Default
  private final int limit = Integer.MAX_VALUE;
}