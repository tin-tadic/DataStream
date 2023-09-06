package com.fsre.streamerapplication.streamer.configuration.jobs;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class SpringBatchDefaultConfig extends DefaultBatchConfigurer {

    @Override
    public void setDataSource(DataSource dataSource) {
      //A hack, with this we are overriding spring  batch configuration, we are using in memory datasource
    }
}
