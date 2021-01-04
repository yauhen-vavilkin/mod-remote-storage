package org.folio.rs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rs.domain.dto.StorageConfiguration;
import org.folio.rs.domain.dto.StorageConfigurations;
import org.folio.rs.domain.entity.Configuration;
import org.folio.rs.error.IdMismatchException;
import org.folio.rs.mapper.ConfigurationsMapper;
import org.folio.rs.repository.ConfigurationsRepository;
import org.folio.spring.data.OffsetRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Log4j2
public class ConfigurationsService {

  private final ConfigurationsRepository configurationsRepository;
  private final ConfigurationsMapper configurationsMapper;

  public void deleteConfigurationById(String configId) {
    var id = UUID.fromString(configId);

    configurationsRepository.deleteById(id);
  }

  public StorageConfiguration getConfigurationById(String configId) {
    var id = UUID.fromString(configId);

    return configurationsRepository.findById(id).map(configurationsMapper::mapEntityToDto).orElse(null);
  }

  public StorageConfigurations getConfigurations(Integer offset, Integer limit) {
    var configurationList = configurationsRepository.findAll(new OffsetRequest(offset, limit));

    return configurationsMapper.mapEntitiesToRemoteConfigCollection(configurationList);
  }

  public StorageConfiguration postConfiguration(StorageConfiguration storageConfiguration) {
    if (isNull(storageConfiguration.getId())) {
      storageConfiguration.id(UUID.randomUUID().toString());
    }
    var configuration = configurationsMapper.mapDtoToEntity(storageConfiguration);
    configuration.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));

    return configurationsMapper.mapEntityToDto(configurationsRepository.save(configuration));
  }

  public void updateConfiguration(String configId, StorageConfiguration storageConfiguration) {
    if (configId.equals(storageConfiguration.getId())) {
      var configuration = configurationsMapper.mapDtoToEntity(storageConfiguration);
      configurationsRepository.save(copyForUpdate(configurationsRepository.getOne(configuration.getId()), configuration));
    } else {
      throw new IdMismatchException();
    }
  }

  private Configuration copyForUpdate(Configuration dest, Configuration source) {
    dest.setProviderName(source.getProviderName());
    dest.setUrl(source.getUrl());
    dest.setAccessionDelay(source.getAccessionDelay());
    dest.setAccessionTimeUnit(source.getAccessionTimeUnit());
    dest.setUpdatedByUserId(source.getUpdatedByUserId());
    dest.setUpdatedByUsername(source.getUpdatedByUsername());
    dest.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
    return dest;
  }
}