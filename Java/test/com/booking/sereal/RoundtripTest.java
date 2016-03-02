package com.booking.sereal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that [thing]->encode->decode->[thing] works successfully for all things.
 */
public class RoundtripTest {

	Encoder encoder;
	Decoder decoder;
	Random rand;

	@Before
	public void setup() {

		encoder = new Encoder( null );
		decoder = new Decoder( null );
		rand = new Random();
	}

	@Test
	public void varint() throws IOException, SerealException {

		int t = 1 * 1000 * 1000; // test a million random ints
		while( t-- > 0 ) {
			int n = rand.nextInt( Integer.MAX_VALUE );
			decoder.setData( encoder.write(n) );
			assertTrue( "Varint not decoded correctly: " + n, ((Long) decoder.decode()) == n );
			encoder.reset();
			decoder.reset();
		}

	}


	@Test
	public void regex() throws IOException {

		Pattern[] patterns = new Pattern[] { Pattern.compile( "foo" ), Pattern.compile( "foo", Pattern.DOTALL ),
				Pattern.compile( "foo", Pattern.DOTALL | Pattern.MULTILINE ), Pattern.compile( "foo", Pattern.DOTALL | Pattern.MULTILINE | Pattern.COMMENTS ),
				Pattern.compile( "foo", Pattern.DOTALL | Pattern.MULTILINE | Pattern.COMMENTS | Pattern.CASE_INSENSITIVE ), Pattern.compile( "(?:foo)" ),
				Pattern.compile( "[0-9]{3}" ), Pattern.compile( "foo(bar)?" ), Pattern.compile( "(foo(bar))" ), };

		for(Pattern p : patterns) {

			try {
				encoder.reset();

				decoder.setData( encoder.write( p ) );
				Pattern actual = (Pattern) decoder.decode();

				Assert.assertEquals( "Pattern not equal: " + p.pattern() + " != " + actual.pattern(), p.pattern(), actual.pattern() );
				Assert.assertEquals( "Flags not equal: " + p.flags() + " != " + actual.flags(), p.flags(), actual.flags() );
			} catch (SerealException e) {
				fail( e.getMessage() );
			}

		}

	}

	@Test
	public void byte_array() throws IOException {

		int n = 10 * 1000;
		while( n-- > 0 ) {
			// make some random bytes
			byte[] pre = new byte[rand.nextInt( 100 )];
            rand.nextBytes( pre );

			try {
				decoder.setData( encoder.write( pre ) );
				Object post = decoder.decode();
                Assert.assertTrue(post instanceof byte[]);
				Assert.assertArrayEquals(pre, (byte[]) post);
			} catch (SerealException e) {
				fail( e.getMessage() );
			}

			encoder.reset();
			decoder.reset();
		}
	}

	@Test
	public void copy() throws IOException {
		// write 3 copies of a string (that should be copied)
		String str = "This is quite a long string";
		try {
			encoder.write( new String[]{str, str, str} );
		} catch (SerealException e) {
			fail( e.getMessage() );
		}
		decoder.setData( encoder.getData() );
		try {
			// read all 3
			Object[] o = (Object[]) decoder.decode();
			assertEquals( "Number of objects", 3, o.length );
			for(Object s : o) {
				assertEquals( str, s );
			}
		} catch (SerealException e) {
			fail( e.getMessage() );
		}

	}

	@Test
	public void short_binary() throws IOException {

		Latin1String str = new Latin1String( "Hello, Sereal!" );
		try {
			encoder.write( str.getBytes() );
		} catch (SerealException e) {
			fail( e.getMessage() );
		}

		decoder.setData( encoder.getData() );
		try {
            Object obj = decoder.decode();
            assertTrue(obj instanceof byte[]);
			assertEquals( str, new Latin1String((byte[]) obj) );
		} catch (SerealException e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void booleans() throws IOException, SerealException {

		encoder.write( true );
		decoder.setData( encoder.getData() );
		try {
			assertTrue( (Boolean) decoder.decode() );
		} catch (SerealException e) {
			fail( e.getMessage() );
		}

		encoder = new Encoder( null );
		encoder.write( false );
		decoder.setData( encoder.getData() );
		try {
			assertFalse( (Boolean) decoder.decode() );
		} catch (SerealException e) {
			fail( e.getMessage() );
		}
	}

}
