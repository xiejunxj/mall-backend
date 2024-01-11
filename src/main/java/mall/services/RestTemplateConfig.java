package mall.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    /**
     * 常用远程调用RestTemplate
     * @return restTemplate
     */
    @Bean("restTemplate")
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    /**
     * 使RestTemplate支持转换类型为text/plain的数据
     */
    public static class WxMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
        public WxMappingJackson2HttpMessageConverter(){
            List<MediaType> mediaTypes = new ArrayList<>();
            // 加入text/plain类型的支持
            mediaTypes.add(MediaType.TEXT_PLAIN);
            // 如果还有其他类型的需要装换，可以一一加上
            setSupportedMediaTypes(mediaTypes);
        }
    }
}