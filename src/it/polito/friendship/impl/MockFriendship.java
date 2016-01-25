package it.polito.friendship.impl;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import it.polito.friendship.Friendship;
import static java.util.Arrays.*;

public class MockFriendship implements Friendship{

	@Override
	public Set<String> getFriends(String userId) {
		return new HashSet<String>(
			asList(
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser(),
				randomUser()
			)
		);
	}
	
	public static String randNum(int min, int max) {
	    return String.valueOf(new Random().nextInt((max - min) + 1) + min);
	}
	
	public static String randomUser(){
		return randNum(0, 1000);
	}
}
