package com.redhat.ipaas.model;

import java.util.Arrays;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.ipaas.model.connection.Connection;
import com.redhat.ipaas.model.integration.Integration;

public class TagFinderTest {

    @Test
    public void findTags() {
        
        Integration integration = new Integration.Builder()
                .tags(new TreeSet<String>(Arrays.asList("tag1", "tag2")))
                .build();
        Connection connection = new Connection.Builder()
                .tags(new TreeSet<String>(Arrays.asList("tag2", "tag3")))
                .build();
        ListResult<String> allTags = new TagFinder()
                .add(ListResult.of(Arrays.asList(integration)))
                .add(ListResult.of(Arrays.asList(connection)))
                .getResult();
        
        Assert.assertEquals( 3, allTags.getTotalCount());
        Assert.assertTrue( allTags.getItems().contains("tag1") );
        Assert.assertTrue( allTags.getItems().contains("tag2") );
        Assert.assertTrue( allTags.getItems().contains("tag3") );
        
    }

}
