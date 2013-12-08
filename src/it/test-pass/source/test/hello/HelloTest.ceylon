import ceylon.test { test, assertEquals }

class Hello() {
	shared String msg = "Hello Ceylon!";
}

class HelloTest() {
	
	shared test void testMsg() {
		value hello = Hello();
		assertEquals(hello.msg, "Hello Ceylon!");
	}
}