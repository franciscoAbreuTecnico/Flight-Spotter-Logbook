package com.flightspotterlogbook.service;

import com.flightspotterlogbook.dto.AirportDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing European airport data for autocomplete functionality.
 * Contains major European airports with their ICAO/IATA codes and coordinates.
 */
@Service
@Slf4j
public class AirportService {

    private final List<AirportDTO> europeanAirports = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Major European airports - organized by country
        // Portugal
        addAirport("LPPT", "LIS", "Lisbon Portela Airport", "Lisbon", "Portugal", 38.7756, -9.1354);
        addAirport("LPPR", "OPO", "Francisco Sá Carneiro Airport", "Porto", "Portugal", 41.2481, -8.6814);
        addAirport("LPFR", "FAO", "Faro Airport", "Faro", "Portugal", 37.0144, -7.9659);
        addAirport("LPMA", "FNC", "Madeira Airport", "Funchal", "Portugal", 32.6979, -16.7745);
        addAirport("LPAZ", "SMA", "Santa Maria Airport", "Azores", "Portugal", 36.9714, -25.1706);
        addAirport("LPPD", "PDL", "João Paulo II Airport", "Ponta Delgada", "Portugal", 37.7412, -25.6979);
        
        // Spain
        addAirport("LEMD", "MAD", "Madrid Barajas Airport", "Madrid", "Spain", 40.4936, -3.5668);
        addAirport("LEBL", "BCN", "Barcelona El Prat Airport", "Barcelona", "Spain", 41.2971, 2.0785);
        addAirport("LEPA", "PMI", "Palma de Mallorca Airport", "Palma", "Spain", 39.5517, 2.7388);
        addAirport("LEMG", "AGP", "Málaga Airport", "Málaga", "Spain", 36.6749, -4.4991);
        addAirport("LEAL", "ALC", "Alicante Airport", "Alicante", "Spain", 38.2822, -0.5582);
        addAirport("LEVC", "VLC", "Valencia Airport", "Valencia", "Spain", 39.4893, -0.4816);
        addAirport("LEZL", "SVQ", "Seville Airport", "Seville", "Spain", 37.4180, -5.8931);
        addAirport("LEBB", "BIO", "Bilbao Airport", "Bilbao", "Spain", 43.3011, -2.9106);
        addAirport("GCTS", "TFS", "Tenerife South Airport", "Tenerife", "Spain", 28.0445, -16.5725);
        addAirport("GCLP", "LPA", "Gran Canaria Airport", "Las Palmas", "Spain", 27.9319, -15.3866);
        addAirport("GCFV", "FUE", "Fuerteventura Airport", "Fuerteventura", "Spain", 28.4527, -13.8638);
        addAirport("GCRR", "ACE", "Lanzarote Airport", "Lanzarote", "Spain", 28.9455, -13.6052);
        
        // United Kingdom
        addAirport("EGLL", "LHR", "London Heathrow Airport", "London", "United Kingdom", 51.4700, -0.4543);
        addAirport("EGKK", "LGW", "London Gatwick Airport", "London", "United Kingdom", 51.1481, -0.1903);
        addAirport("EGSS", "STN", "London Stansted Airport", "London", "United Kingdom", 51.8850, 0.2350);
        addAirport("EGLC", "LCY", "London City Airport", "London", "United Kingdom", 51.5053, 0.0553);
        addAirport("EGGW", "LTN", "London Luton Airport", "London", "United Kingdom", 51.8747, -0.3683);
        addAirport("EGCC", "MAN", "Manchester Airport", "Manchester", "United Kingdom", 53.3537, -2.2750);
        addAirport("EGBB", "BHX", "Birmingham Airport", "Birmingham", "United Kingdom", 52.4539, -1.7480);
        addAirport("EGPH", "EDI", "Edinburgh Airport", "Edinburgh", "United Kingdom", 55.9500, -3.3725);
        addAirport("EGPF", "GLA", "Glasgow Airport", "Glasgow", "United Kingdom", 55.8719, -4.4331);
        addAirport("EGGD", "BRS", "Bristol Airport", "Bristol", "United Kingdom", 51.3827, -2.7191);
        
        // France
        addAirport("LFPG", "CDG", "Paris Charles de Gaulle Airport", "Paris", "France", 49.0097, 2.5479);
        addAirport("LFPO", "ORY", "Paris Orly Airport", "Paris", "France", 48.7233, 2.3794);
        addAirport("LFML", "MRS", "Marseille Provence Airport", "Marseille", "France", 43.4393, 5.2214);
        addAirport("LFLL", "LYS", "Lyon Saint-Exupéry Airport", "Lyon", "France", 45.7256, 5.0811);
        addAirport("LFMN", "NCE", "Nice Côte d'Azur Airport", "Nice", "France", 43.6584, 7.2159);
        addAirport("LFBO", "TLS", "Toulouse Blagnac Airport", "Toulouse", "France", 43.6293, 1.3638);
        addAirport("LFBD", "BOD", "Bordeaux Mérignac Airport", "Bordeaux", "France", 44.8283, -0.7156);
        addAirport("LFRS", "NTE", "Nantes Atlantique Airport", "Nantes", "France", 47.1532, -1.6107);
        
        // Germany
        addAirport("EDDF", "FRA", "Frankfurt Airport", "Frankfurt", "Germany", 50.0379, 8.5622);
        addAirport("EDDM", "MUC", "Munich Airport", "Munich", "Germany", 48.3538, 11.7861);
        addAirport("EDDB", "BER", "Berlin Brandenburg Airport", "Berlin", "Germany", 52.3667, 13.5033);
        addAirport("EDDL", "DUS", "Düsseldorf Airport", "Düsseldorf", "Germany", 51.2895, 6.7668);
        addAirport("EDDH", "HAM", "Hamburg Airport", "Hamburg", "Germany", 53.6304, 9.9882);
        addAirport("EDDK", "CGN", "Cologne Bonn Airport", "Cologne", "Germany", 50.8659, 7.1427);
        addAirport("EDDS", "STR", "Stuttgart Airport", "Stuttgart", "Germany", 48.6899, 9.2220);
        addAirport("EDDW", "BRE", "Bremen Airport", "Bremen", "Germany", 53.0475, 8.7867);
        addAirport("EDDN", "NUE", "Nuremberg Airport", "Nuremberg", "Germany", 49.4987, 11.0669);
        addAirport("EDDV", "HAJ", "Hanover Airport", "Hanover", "Germany", 52.4611, 9.6850);
        
        // Italy
        addAirport("LIRF", "FCO", "Rome Fiumicino Airport", "Rome", "Italy", 41.8003, 12.2389);
        addAirport("LIMC", "MXP", "Milan Malpensa Airport", "Milan", "Italy", 45.6306, 8.7281);
        addAirport("LIME", "BGY", "Milan Bergamo Airport", "Bergamo", "Italy", 45.6739, 9.7042);
        addAirport("LIML", "LIN", "Milan Linate Airport", "Milan", "Italy", 45.4451, 9.2768);
        addAirport("LIPZ", "VCE", "Venice Marco Polo Airport", "Venice", "Italy", 45.5053, 12.3519);
        addAirport("LIRN", "NAP", "Naples Airport", "Naples", "Italy", 40.8860, 14.2908);
        addAirport("LIPE", "BLQ", "Bologna Airport", "Bologna", "Italy", 44.5354, 11.2887);
        addAirport("LICC", "CTA", "Catania Airport", "Catania", "Italy", 37.4668, 15.0664);
        addAirport("LICJ", "PMO", "Palermo Airport", "Palermo", "Italy", 38.1760, 13.0910);
        addAirport("LIRP", "PSA", "Pisa Airport", "Pisa", "Italy", 43.6839, 10.3927);
        
        // Netherlands
        addAirport("EHAM", "AMS", "Amsterdam Schiphol Airport", "Amsterdam", "Netherlands", 52.3086, 4.7639);
        addAirport("EHEH", "EIN", "Eindhoven Airport", "Eindhoven", "Netherlands", 51.4501, 5.3743);
        addAirport("EHRD", "RTM", "Rotterdam The Hague Airport", "Rotterdam", "Netherlands", 51.9569, 4.4372);
        
        // Belgium
        addAirport("EBBR", "BRU", "Brussels Airport", "Brussels", "Belgium", 50.9014, 4.4844);
        addAirport("EBCI", "CRL", "Brussels South Charleroi Airport", "Charleroi", "Belgium", 50.4592, 4.4538);
        
        // Switzerland
        addAirport("LSZH", "ZRH", "Zurich Airport", "Zurich", "Switzerland", 47.4647, 8.5492);
        addAirport("LSGG", "GVA", "Geneva Airport", "Geneva", "Switzerland", 46.2381, 6.1089);
        addAirport("LSZB", "BRN", "Bern Airport", "Bern", "Switzerland", 46.9141, 7.4972);
        addAirport("LSZA", "LUG", "Lugano Airport", "Lugano", "Switzerland", 46.0040, 8.9106);
        
        // Austria
        addAirport("LOWW", "VIE", "Vienna International Airport", "Vienna", "Austria", 48.1103, 16.5697);
        addAirport("LOWG", "GRZ", "Graz Airport", "Graz", "Austria", 46.9911, 15.4396);
        addAirport("LOWS", "SZG", "Salzburg Airport", "Salzburg", "Austria", 47.7933, 13.0043);
        addAirport("LOWI", "INN", "Innsbruck Airport", "Innsbruck", "Austria", 47.2602, 11.3439);
        
        // Ireland
        addAirport("EIDW", "DUB", "Dublin Airport", "Dublin", "Ireland", 53.4213, -6.2701);
        addAirport("EICK", "ORK", "Cork Airport", "Cork", "Ireland", 51.8413, -8.4911);
        addAirport("EINN", "SNN", "Shannon Airport", "Shannon", "Ireland", 52.7020, -8.9248);
        
        // Nordic countries
        addAirport("EKCH", "CPH", "Copenhagen Airport", "Copenhagen", "Denmark", 55.6180, 12.6508);
        addAirport("EKBI", "BLL", "Billund Airport", "Billund", "Denmark", 55.7403, 9.1518);
        addAirport("ENGM", "OSL", "Oslo Gardermoen Airport", "Oslo", "Norway", 60.1939, 11.1004);
        addAirport("ENBR", "BGO", "Bergen Airport", "Bergen", "Norway", 60.2934, 5.2181);
        addAirport("ESSA", "ARN", "Stockholm Arlanda Airport", "Stockholm", "Sweden", 59.6519, 17.9186);
        addAirport("ESGG", "GOT", "Gothenburg Landvetter Airport", "Gothenburg", "Sweden", 57.6628, 12.2798);
        addAirport("EFHK", "HEL", "Helsinki Vantaa Airport", "Helsinki", "Finland", 60.3172, 24.9633);
        addAirport("BIKF", "KEF", "Keflavik International Airport", "Reykjavik", "Iceland", 63.9850, -22.6056);
        
        // Eastern Europe
        addAirport("EPWA", "WAW", "Warsaw Chopin Airport", "Warsaw", "Poland", 52.1657, 20.9671);
        addAirport("EPKK", "KRK", "Kraków John Paul II Airport", "Kraków", "Poland", 50.0777, 19.7848);
        addAirport("EPGD", "GDN", "Gdańsk Lech Wałęsa Airport", "Gdańsk", "Poland", 54.3776, 18.4662);
        addAirport("LKPR", "PRG", "Prague Václav Havel Airport", "Prague", "Czech Republic", 50.1008, 14.2600);
        addAirport("LHBP", "BUD", "Budapest Ferenc Liszt Airport", "Budapest", "Hungary", 47.4298, 19.2611);
        addAirport("LROP", "OTP", "Bucharest Henri Coandă Airport", "Bucharest", "Romania", 44.5711, 26.0850);
        addAirport("LBSF", "SOF", "Sofia Airport", "Sofia", "Bulgaria", 42.6952, 23.4062);
        addAirport("LWSK", "SKP", "Skopje International Airport", "Skopje", "North Macedonia", 41.9616, 21.6214);
        addAirport("LYBE", "BEG", "Belgrade Nikola Tesla Airport", "Belgrade", "Serbia", 44.8184, 20.3091);
        addAirport("LDZA", "ZAG", "Zagreb Airport", "Zagreb", "Croatia", 45.7429, 16.0688);
        addAirport("LDSP", "SPU", "Split Airport", "Split", "Croatia", 43.5389, 16.2980);
        addAirport("LDDU", "DBV", "Dubrovnik Airport", "Dubrovnik", "Croatia", 42.5614, 18.2682);
        // addAirport("LJLJ", "LJU", "Ljubljana Joze Pucnik Airport", "Ljubljana", "Slovenia", 46.2237, 14.4576);
        
        // Greece & Cyprus
        addAirport("LGAV", "ATH", "Athens International Airport", "Athens", "Greece", 37.9364, 23.9445);
        addAirport("LGTS", "SKG", "Thessaloniki Airport", "Thessaloniki", "Greece", 40.5197, 22.9709);
        addAirport("LGIR", "HER", "Heraklion Airport", "Heraklion", "Greece", 35.3397, 25.1803);
        addAirport("LGSR", "JTR", "Santorini Airport", "Santorini", "Greece", 36.3992, 25.4793);
        addAirport("LGMK", "JMK", "Mykonos Airport", "Mykonos", "Greece", 37.4351, 25.3481);
        addAirport("LGRP", "RHO", "Rhodes International Airport", "Rhodes", "Greece", 36.4054, 28.0862);
        addAirport("LGKO", "KGS", "Kos Airport", "Kos", "Greece", 36.7933, 26.9402);
        addAirport("LGKR", "CFU", "Corfu Airport", "Corfu", "Greece", 39.6019, 19.9117);
        addAirport("LCLK", "LCA", "Larnaca International Airport", "Larnaca", "Cyprus", 34.8751, 33.6249);
        addAirport("LCPH", "PFO", "Paphos International Airport", "Paphos", "Cyprus", 34.7180, 32.4857);
        
        // Turkey (European part / major hubs)
        addAirport("LTFM", "IST", "Istanbul Airport", "Istanbul", "Turkey", 41.2753, 28.7519);
        addAirport("LTFJ", "SAW", "Istanbul Sabiha Gökçen Airport", "Istanbul", "Turkey", 40.8986, 29.3092);
        addAirport("LTAI", "AYT", "Antalya Airport", "Antalya", "Turkey", 36.8987, 30.8005);
        addAirport("LTBA", "ISL", "Istanbul Atatürk Airport", "Istanbul", "Turkey", 40.9769, 28.8146);
        addAirport("LTFE", "ADB", "İzmir Adnan Menderes Airport", "Izmir", "Turkey", 38.2924, 27.1570);
        addAirport("LTBJ", "DLM", "Dalaman Airport", "Dalaman", "Turkey", 36.7131, 28.7925);
        addAirport("LTBS", "BJV", "Bodrum Milas Airport", "Bodrum", "Turkey", 37.2506, 27.6643);
        
        // Baltic states
        addAirport("EVRA", "RIX", "Riga International Airport", "Riga", "Latvia", 56.9236, 23.9711);
        addAirport("EETN", "TLL", "Tallinn Airport", "Tallinn", "Estonia", 59.4133, 24.8328);
        addAirport("EYVI", "VNO", "Vilnius Airport", "Vilnius", "Lithuania", 54.6341, 25.2858);
        
        // Malta & Luxembourg
        addAirport("LMML", "MLA", "Malta International Airport", "Valletta", "Malta", 35.8575, 14.4775);
        addAirport("ELLX", "LUX", "Luxembourg Airport", "Luxembourg", "Luxembourg", 49.6233, 6.2044);
        
        log.info("Loaded {} European airports", europeanAirports.size());
    }

    private void addAirport(String icao, String iata, String name, String city, String country, double lat, double lon) {
        europeanAirports.add(AirportDTO.builder()
                .icao(icao)
                .iata(iata)
                .name(name)
                .city(city)
                .country(country)
                .latitude(lat)
                .longitude(lon)
                .build());
    }

    /**
     * Search airports by query string. Matches against ICAO, IATA, name, or city.
     */
    public List<AirportDTO> searchAirports(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        String lowerQuery = query.toLowerCase().trim();
        return europeanAirports.stream()
                .filter(a -> 
                    a.getIcao().toLowerCase().contains(lowerQuery) ||
                    a.getIata().toLowerCase().contains(lowerQuery) ||
                    a.getName().toLowerCase().contains(lowerQuery) ||
                    a.getCity().toLowerCase().contains(lowerQuery) ||
                    a.getCountry().toLowerCase().contains(lowerQuery)
                )
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * Get airport by ICAO or IATA code.
     */
    public AirportDTO getAirportByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String upperCode = code.toUpperCase().trim();
        return europeanAirports.stream()
                .filter(a -> a.getIcao().equals(upperCode) || a.getIata().equals(upperCode))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all airports.
     */
    public List<AirportDTO> getAllAirports() {
        return new ArrayList<>(europeanAirports);
    }
}
