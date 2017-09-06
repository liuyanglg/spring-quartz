package com.lyf.timer.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 */
public class PropertyConfigurer extends PropertyPlaceholderConfigurer {
    public static Logger LOG = Logger.getLogger(PropertyConfigurer.class);
    private Resource[] locations;

    @Override
    protected void loadProperties(Properties props) throws IOException {
        final String k = "97DC0D40FCFB425EA2A94C3B34ED99F9";
        if (this.locations != null) {

            PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

            for (Resource r : locations) {
                if ("jdbc-decrypt.properties".equals(r.getFilename())) {
                    InputStream input = r.getInputStream();
                    byte[] b = new byte[input.available()];
                    EndeUtil ss = new EndeUtil();

                    input.read(b);
                    input.close();
                    String samdata = new String(b).toString();
                    String jmdata = ss.encrypt(samdata);
                    LOG.info("加密后：" + jmdata);
                    System.out.println("加密后：" + jmdata);
                    try {


                        byte[] sjmdata = jmdata.getBytes();

                        //propertiesPersister.load(props, new ByteArrayInputStream(des.decrypt);

                        propertiesPersister.load(props, new ByteArrayInputStream(sjmdata));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
                if ("jdbc.properties".equals(r.getFilename())) {
                    InputStream input = r.getInputStream();
                    byte[] b = new byte[input.available()];
                    EndeUtil ss = new EndeUtil();

                    input.read(b);
                    input.close();
                    String samdata = new String(b).toString();
                    String jmdata = ss.decrypt(samdata);
                    System.out.println("解密后："+jmdata);
                    try {


                        byte[] sjmdata = jmdata.getBytes();

                        //propertiesPersister.load(props, new ByteArrayInputStream(des.decrypt);

                        propertiesPersister.load(props, new ByteArrayInputStream(sjmdata));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    InputStream input = r.getInputStream();
                    propertiesPersister.load(props, input);
                    input.close();
                }
            }
        }
    }
    //   }


    @Override
    public void setLocations(Resource[] locations) {
        this.locations = locations;
    }


}



