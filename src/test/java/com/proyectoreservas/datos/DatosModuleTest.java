package com.proyectoreservas.datos;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoreservas.application.datos.*;
import com.proyectoreservas.domain.datos.*;
import com.proyectoreservas.infrastructure.datos.DuckDbAnalyticsAdapter;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class DatosModuleTest {
  @Test void circuitOpensAfterThreeFailures() throws Exception {
    var cb=new CircuitBreaker(3,Duration.ofSeconds(30),Clock.systemUTC());
    for(int i=0;i<3;i++) assertThrows(Exception.class, ()->cb.execute(()->{throw new Exception("offline");}));
    assertEquals(CircuitBreaker.State.OPEN,cb.state());
    assertThrows(IllegalStateException.class,()->cb.execute(()->"ok"));
  }
  @Test void circuitClosesAfterSuccessfulCall() throws Exception {
    var cb=new CircuitBreaker(1,Duration.ZERO,Clock.systemUTC());
    assertThrows(Exception.class,()->cb.execute(()->{throw new Exception("fail");}));
    assertEquals("ok",cb.execute(()->"ok"));
    assertEquals(CircuitBreaker.State.CLOSED,cb.state());
  }
  @Test void injectedPortsAndDuckDb() throws Exception {
    var mapper=new ObjectMapper();
    var service=new OpenDataService((id,limit)->mapper.readTree("[{\"nombre_del_departamento\":\"Bogotá\"},{\"nombre_del_departamento\":\"Bogotá\"}]"));
    var rows=service.fetch("n5yy-8nav",2);
    var analytics=new AnalyticsService(new DuckDbAnalyticsAdapter());
    var result=analytics.summarize(rows);
    assertFalse(result.isEmpty());
    assertEquals("estadisticas",result.get(0).get("consulta"));
    assertThrows(IllegalArgumentException.class,()->service.fetch("invalido",2));
  }
}