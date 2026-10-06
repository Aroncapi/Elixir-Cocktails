package com.barpro.core.repository;

import com.barpro.core.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
