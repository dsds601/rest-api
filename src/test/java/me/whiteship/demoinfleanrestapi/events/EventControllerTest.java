package me.whiteship.demoinfleanrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whiteship.demoinfleanrestapi.common.RestDocsConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
 
    @Autowired
    EventRepository eventRepository;

    @Test
    @DisplayName("??????????????? ????????? ?????? ?????????")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021,02,11,17,10))
                .closeEnrollmentDateTime(LocalDateTime.of(2021,02,21,17,10))
                .beginEventDateTime(LocalDateTime.of(2021,02,22,17,10))
                .endEventDateTime(LocalDateTime.of(2021,02,24,18,10))
                .basePrice(100)
                .maxPrice(210)
                .limitOfEnrollment(100)
                .location("????????? D2 ????????? ?????????")
                .build();

        mockMvc.perform(post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an exist")
                                ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("data time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("data time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("data time of beginEvent of new event"),
                                fieldWithPath("endEventDateTime").description("data time of endEventDate of new event"),
                                fieldWithPath("location").description("basePrice of new event"),
                                fieldWithPath("basePrice").description("data time of begin of new event"),
                                fieldWithPath("maxPrice").description("maxPrice of new event"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event")
                        )
                ));
    }

    @Test
    @DisplayName("?????? ?????? ??? ?????? ?????? ???????????? ????????? ????????? ???????????? ?????????")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021,02,11,17,10))
                .closeEnrollmentDateTime(LocalDateTime.of(2021,02,21,17,10))
                .endEventDateTime(LocalDateTime.of(2021,02,24,18,10))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("????????? D2 ????????? ?????????")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("?????? ?????? ???????????? ????????? ????????? ???????????? ?????????")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();
        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(this.objectMapper.writeValueAsString(eventDto))
                ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("?????? ?????? ????????? ????????? ????????? ???????????? ?????????")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021,02,25,17,10))
                .closeEnrollmentDateTime(LocalDateTime.of(2021,02,21,17,10))
                .beginEventDateTime(LocalDateTime.of(2021,02,26,17,10))
                .endEventDateTime(LocalDateTime.of(2021,02,24,18,10))
                .basePrice(100000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("????????? D2 ????????? ?????????")
                .build();
        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].ObjectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists());
    }

    @Test
    @DisplayName("30?????? ???????????? 10?????? ????????? ????????? ????????????")
    public void queryEvents() throws Exception{
        //Given
//        IntStream.range(0,30).forEach(i-> {
//            this.generateEvent(i);
//        });
        IntStream.range(0,30).forEach(this::generateEvent);
        //When
        this.mockMvc.perform(get("/api/events")
                        .param("page","1")
                        .param("size","10")
                        .param("sort","name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andDo(document("query-events"));

    }

    private void generateEvent(int index) {
        Event event = Event.builder()
                .name("event " +index)
                .description("test event")
                .build();
        this.eventRepository.save(event);
    }
}
