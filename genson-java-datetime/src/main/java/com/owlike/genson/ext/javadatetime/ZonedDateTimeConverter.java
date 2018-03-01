package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class ZonedDateTimeConverter extends BaseTemporalAccessorConverter<ZonedDateTime> {
	ZonedDateTimeConverter(DateTimeConverterOptions options) {
		super(options, new ZonedDateTimeTimestampHandler(options), ZonedDateTime::from);
	}

	private static class ZonedDateTimeTimestampHandler extends TimestampHandler<ZonedDateTime> {
		private static final String ZONE_ID_FIELD_NAME = "zoneId";
		private static final LinkedHashMap<String, TemporalField> ZONED_DATE_TIME_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			ZONED_DATE_TIME_TEMPORAL_FIELDS.put("year", ChronoField.YEAR);
			ZONED_DATE_TIME_TEMPORAL_FIELDS.put("month", ChronoField.MONTH_OF_YEAR);
			ZONED_DATE_TIME_TEMPORAL_FIELDS.put("day", ChronoField.DAY_OF_MONTH);
			ZONED_DATE_TIME_TEMPORAL_FIELDS.put("hour", ChronoField.HOUR_OF_DAY);
			ZONED_DATE_TIME_TEMPORAL_FIELDS.put("minute", ChronoField.MINUTE_OF_HOUR);
			ZONED_DATE_TIME_TEMPORAL_FIELDS.put("second", ChronoField.SECOND_OF_MINUTE);
			ZONED_DATE_TIME_TEMPORAL_FIELDS.put("nano", ChronoField.NANO_OF_SECOND);
		}

		ZonedDateTimeTimestampHandler(DateTimeConverterOptions options) {
			super(zt -> DateTimeUtil.instantToMillis(zt.toInstant()),
					millis -> ZonedDateTime.ofInstant(DateTimeUtil.instantFromMillis(millis), options.getZoneId()),
					zt -> DateTimeUtil.instantToNanos(zt.toInstant()),
					nanos -> ZonedDateTime.ofInstant(DateTimeUtil.instantFromNanos(nanos), options.getZoneId()),
					ZONED_DATE_TIME_TEMPORAL_FIELDS, ZonedDateTime::now);
		}

		@Override
		protected ZonedDateTime readFieldFromObject(ZonedDateTime obj, ObjectReader reader) {
			if(reader.name().equals(ZONE_ID_FIELD_NAME)){
				String zoneIdValue = reader.valueAsString();
				ZoneId zoneId = ZoneId.of(zoneIdValue);
				return obj.withZoneSameLocal(zoneId);
			}
			else {
				return super.readFieldFromObject(obj, reader);
			}
		}

		@Override
		protected void writeFieldsAsObject(ZonedDateTime object, ObjectWriter writer) {
			super.writeFieldsAsObject(object, writer);
			writer.writeName(ZONE_ID_FIELD_NAME).writeValue(object.getZone().getId());
		}

		@Override
		public ZonedDateTime readFieldsFromArray(Supplier<ZonedDateTime> instanceProvider, ObjectReader reader) {
			ZonedDateTime zt = super.readFieldsFromArray(instanceProvider, reader);
			reader.next();
			String zoneId = reader.valueAsString();
			return zt.withZoneSameLocal(ZoneId.of(zoneId));
		}

		@Override
		protected void writeFieldsAsArray(ZonedDateTime object, ObjectWriter writer) {
			super.writeFieldsAsArray(object, writer);
			writer.writeValue(object.getZone().getId());
		}
	}
}
