package shop.wannab.userservice.point.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import shop.wannab.userservice.point.domain.dto.request.PointHistoryCreateDTO;
import shop.wannab.userservice.point.service.PointHistoryService;

import static shop.wannab.userservice.global.config.RabbitMQConfig.ORDER_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointProcessHandler {
    private final ObjectMapper objectMapper;
    private final PointHistoryService pointHistoryService;

    @PostConstruct
    public void setObjectMapper() {
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @RabbitListener(queues = ORDER_QUEUE)
    public void handleOrderPointProcess(String pointHistoryCreationDtoPayload) throws JsonProcessingException {
        try {
            String json = objectMapper.readValue(pointHistoryCreationDtoPayload, String.class);
            PointHistoryCreateDTO pointHistoryCreateDTO = objectMapper.readValue(json, PointHistoryCreateDTO.class);
            pointHistoryService.createPointHistory(pointHistoryCreateDTO);
        } catch (Exception e) {
            log.info("포인트 처리 실패");
        }

    }
}
