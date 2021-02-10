package org.folio.rs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.rs.client.CirculationClient;
import org.folio.rs.client.LocationClient;
import org.folio.rs.domain.dto.CheckInCirculationRequest;
import org.folio.rs.domain.dto.CheckInItem;
import org.folio.rs.repository.LocationMappingsRepository;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.joda.time.DateTimeZone.UTC;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckInItemService {

  private final CirculationClient circulationClient;
  private final LocationMappingsRepository locationMappingsRepository;
  private final LocationClient locationClient;

  public HttpStatus checkInItemByBarcode(String remoteStorageConfigurationId, CheckInItem checkInItem) {
    log.info("Start check-in process for item with barcode " + checkInItem.getItemBarcode());
    var location = locationMappingsRepository
      .getFirstByConfigurationId(UUID.fromString(remoteStorageConfigurationId));
    if (location.isPresent()) {
      var folioLocationId = location.get().getFolioLocationId().toString();
      var folioLocation = locationClient.getLocation(folioLocationId);
      if (StringUtils.isNotBlank(folioLocation.getPrimaryServicePoint())) {
        var responseCirculation = circulationClient.checkIn(
          CheckInCirculationRequest.of(checkInItem.getItemBarcode(), folioLocation.getPrimaryServicePoint(), DateTime.now(UTC)));
        if (responseCirculation.getStatusCode() == HttpStatus.OK) {
          log.info("Check-in success for item with barcode " + checkInItem.getItemBarcode());
          return HttpStatus.OK;
        }
      }
    }
    log.error("Check-in call error for item with barcode " + checkInItem.getItemBarcode());
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
