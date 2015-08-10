package com.crespo.ignacio.depchecker;

import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class AsmUtilsTest extends TestCase {

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testExtractTypesFromDescEmpty() {
        assertEquals(0, AsmUtils.extractTypesFromDesc(null).size());
        assertEquals(0, AsmUtils.extractTypesFromDesc("").size());
    }

    @Test
    public void testExtractTypesFromDescOne() {
        final Set<String> types = AsmUtils.extractTypesFromDesc("Lch/ucid/ui/FragmentRunnable;");
        assertEquals(1, types.size());
        assertTrue(types.contains("ch/ucid/ui/FragmentRunnable"));
    }

    @Test
    public void testExtractTypesFromMethodSignature() {
        final Set<String> types = AsmUtils.extractTypesFromDesc("(Lch/ucid/ui/FragmentRunnable;)Ljava/lang/Object;");
        assertEquals(2, types.size());
        assertTrue(types.contains("ch/ucid/ui/FragmentRunnable"));
        assertTrue(types.contains("java/lang/Object"));
    }

    @Test
    public void testExtractTypesFromMethodSignatureNoParameters() {
        final Set<String> types = AsmUtils.extractTypesFromDesc("()Ljava/lang/Object;");
        assertEquals(1, types.size());
        assertTrue(types.contains("java/lang/Object"));
    }

    @Test
    public void testExtractTypesFromDescMore() {
        final Set<String> types = AsmUtils
                .extractTypesFromDesc("Lch/ucid/ui/FragmentRunnable;Lch/ucid/call/ui/CallScreenFragment;Ljava/lang/Object;");
        assertEquals(3, types.size());
        assertTrue(types.contains("ch/ucid/ui/FragmentRunnable"));
        assertTrue(types.contains("ch/ucid/call/ui/CallScreenFragment"));
        assertTrue(types.contains("java/lang/Object"));
    }

    @Test
    public void testExtractTypesFromDescMoreWithPrimitives() {
        final Set<String> types = AsmUtils
                .extractTypesFromDesc("ZLch/ucid/ui/FragmentRunnable;IIILch/ucid/call/ui/CallScreenFragment;ZBLjava/lang/Object;I");
        assertEquals(3, types.size());
        assertTrue(types.contains("ch/ucid/ui/FragmentRunnable"));
        assertTrue(types.contains("ch/ucid/call/ui/CallScreenFragment"));
        assertTrue(types.contains("java/lang/Object"));
    }

    @Test
    public void testExtractTypesFromDescGenerics() {
        final Set<String> types = AsmUtils.extractTypesFromDesc("Lch/ucid/ui/FragmentRunnable<Lch/ucid/call/ui/CallScreenFragment;>;");
        assertEquals(2, types.size());
        assertTrue(types.contains("ch/ucid/ui/FragmentRunnable"));
        assertTrue(types.contains("ch/ucid/call/ui/CallScreenFragment"));
    }

    @Test
    public void testExtractTypesFromDescDoubleGeneric() {
        final Set<String> types = AsmUtils.extractTypesFromDesc("Ljava/lang/Object;Ljava/util/Comparator<Lch/ucid/groupchat/ui/ParticipantItem;>;");
        assertEquals(3, types.size());
        assertTrue(types.contains("java/lang/Object"));
        assertTrue(types.contains("java/util/Comparator"));
        assertTrue(types.contains("ch/ucid/groupchat/ui/ParticipantItem"));
    }

    @Test
    public void testExtractTypesFromDescMultiple() {
        final Set<String> types = AsmUtils
                .extractTypesFromDesc("Lch/ucid/controller/ondemand/OnDemandData<Lch/ucid/contacts/avatars/AvatarKey;Lch/ucid/contacts/avatars/Avatar;>;Lch/ucid/events/EventListener;");
        assertEquals(4, types.size());
        assertTrue(types.contains("ch/ucid/controller/ondemand/OnDemandData"));
        assertTrue(types.contains("ch/ucid/contacts/avatars/AvatarKey"));
        assertTrue(types.contains("ch/ucid/contacts/avatars/Avatar"));
        assertTrue(types.contains("ch/ucid/events/EventListener"));
    }

    @Test
    public void testExtractTypesFromDescWithExtends() {
        final Set<String> types = AsmUtils.extractTypesFromDesc("<T::Lch/ucid/core/history/IHistoryEntry;>Lch/ucid/storage/sql/BaseSqlDao<TT;>;");
        assertEquals(2, types.size());
        assertTrue(types.contains("ch/ucid/core/history/IHistoryEntry"));
        assertTrue(types.contains("ch/ucid/storage/sql/BaseSqlDao"));
    }

    @Test
    public void testExtractTypesFromDescGenericsInside() {
        final Set<String> types = AsmUtils
                .extractTypesFromDesc("<Lch/ucid/core/history/IHistoryEntry<Ljava/lang/String;>;>Lch/ucid/storage/sql/BaseSqlDao;");
        assertEquals(3, types.size());
        assertTrue(types.contains("ch/ucid/core/history/IHistoryEntry"));
        assertTrue(types.contains("ch/ucid/storage/sql/BaseSqlDao"));
        assertTrue(types.contains("java/lang/String"));
    }

}
