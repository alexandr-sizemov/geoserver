package it.polito.util;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class Utils {

	public static String objectToJSONString(Object o){
		GeoObjectMapper mapper = new GeoObjectMapper();
		try {
			return mapper.defaultPrettyPrintingWriter().writeValueAsString(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class GeoObjectMapper extends ObjectMapper {

		public GeoObjectMapper() {
			super();
			setNonNullInclusion();
		}

		public GeoObjectMapper(JsonFactory jf, SerializerProvider sp, DeserializerProvider dp, SerializationConfig sconfig,
				DeserializationConfig dconfig) {
			super(jf, sp, dp, sconfig, dconfig);
			setNonNullInclusion();
		}

		public GeoObjectMapper(JsonFactory jf, SerializerProvider sp, DeserializerProvider dp) {
			super(jf, sp, dp);
			setNonNullInclusion();
		}

		public GeoObjectMapper(JsonFactory jf) {
			super(jf);
			setNonNullInclusion();
		}
		
		public void setNonNullInclusion(){
			getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
		}

	}
	
}
