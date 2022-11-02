import org.junit.Assert;
import org.junit.Test;
import java.util.function.BiFunction;
import java.lang.Math;

import static edu.gvsu.dlunit.DLUnit.*;

/**
 *
 * Group Members: Jochen Tamang
 *
 */
public class Sample16BitALUTest {

  public static class OpCodes {
    public static final int ADDU = 0;
    public static final int SUBU = 1;
    public static final int AND  = 2;
    public static final int OR   = 3;
    public static final int NOT  = 4;
    public static final int XOR  = 5;
    public static final int LUI  = 6;
    public static final int SLTU = 7;
    public static final int ADD  = 8;
    public static final int SUB  = 9;
    public static final int SLT  = 15;
  }

  /**
   * Used for signed and unsigned operators
   */
  public static final long testSigned[] = {-32768, -32767,-16384,-17,-16,-3,-2,-1, 0, 1, 2, 13, 127, 128, 129, 8000, 16000, 0x5555, 32766, 32767};
  public static final long testUnSigned[] = { 0, 1, 2, 13, 127, 128, 129, 8000, 16000, 0x5555, 32766, 32767,65534, 65535};

  private void verifyAddSubtractSigned(long a, long b, int op)
  {
    long expected;
    if(op == 8)
    {
      expected = a + b;
    }
    else
    {
      expected = a-b;
    }
    // add or subtract as specified by `op`

    boolean expectedOverflow = ((expected >= (1 << 15)) || (expected < -(1 << 15)));
    if (expectedOverflow && expected > 0) {
      expected -= 65536;
    } else if (expectedOverflow && expected < 0) {
      expected += 65536;
    }
    setPinSigned("InputA", a);
    setPinSigned("InputB", b);
    setPinUnsigned("Op", op);
    run();
    Assert.assertEquals("Output ", expected, readPinSigned("Output"));
    Assert.assertEquals("Overflow " , expectedOverflow, readPin("Overflow"));
  }
  @Test
  public void testAddSubSigned() {
    for (long a : testSigned) {
      for (long b : testSigned) {
        verifyAddSubtractSigned(a, b, OpCodes.ADD);
        verifyAddSubtractSigned(a, b, OpCodes.SUB);
      }
    }
  }

  private void verifyAddSubtractUnsigned(long a, long b, int op)
  {
    long expected;
    boolean operation;
    if(op == 0)
    {
      expected = (a + b) % 65536;
      operation = false;
    }
    else {
      expected = Math.floorMod(a - b, 65536);
      operation = true;
    }

    setPinUnsigned("InputA", a);
    setPinUnsigned("InputB", b);
    setPinUnsigned("Op", op);
    run();
    String message = "of " + a + (operation ? " - " : " + ") + b + ": ";
    Assert.assertEquals("Output " + message , expected, readPinUnsigned("Output"));
    Assert.assertEquals("Overflow " , false, readPin("Overflow"));
  }
  @Test
  public void testAddSubUnsigned() {
    for (long a : testUnSigned) {
      for (long b : testUnSigned) {
        verifyAddSubtractUnsigned(a, b, OpCodes.ADDU);
        verifyAddSubtractUnsigned(a, b, OpCodes.SUBU);
      }
    }
  }

  // Helper method that runs a test for a given pair of integers and an operation (`false` for add, `true` for subtract)
  @Test
  public void testAddu() {
    setPinUnsigned("InputA", 53400);
    setPinUnsigned("InputB", 53500);
    setPinUnsigned("Op", OpCodes.ADDU);
    run();
    Assert.assertEquals("Addition Output", (53400 + 53500) % 65536, readPinUnsigned("Output"));

    // Overflow for unsigned addition is false by definition
    Assert.assertEquals("Addition Overflow", false, readPin("Overflow"));
  }

  @Test
  public void testAddition() {
    setPinSigned("InputA", 23);
    setPinSigned("InputB", 44);
    setPinUnsigned("Op", OpCodes.ADD);
    run();
    Assert.assertEquals("Addition Output", 23 + 44, readPinSigned("Output"));
    Assert.assertEquals("Addition Overflow", false, readPin("Overflow"));
  }

  @Test
  public void testSubtraction() {
    setPinSigned("InputA", 24);
    setPinSigned("InputB", 45);
    setPinUnsigned("Op", OpCodes.SUB);
    run();
    Assert.assertEquals("Subtraction Output", 24 - 45, readPinSigned("Output"));
    Assert.assertEquals("Subtraction Overflow", false, readPin("Overflow"));
  }

  @Test
  public void ltSigned() {
    setPinSigned("InputA", 5);
    setPinSigned("InputB", 6);
    setPinUnsigned("Op", OpCodes.SLT);
    run();
    Assert.assertEquals("Signed Less Than Output", 1, readPinSigned("Output"));
    Assert.assertEquals("Signed Less Than Overflow", false, readPin("Overflow"));
  }

  @Test
  public void ltSigned2() {
    setPinSigned("InputA", 32767);
    setPinSigned("InputB", -1);
    setPinUnsigned("Op", OpCodes.SLT);
    run();
    Assert.assertEquals("Signed Less Than Output", 0, readPinSigned("Output"));
    Assert.assertEquals("Signed Less Than Overflow", false, readPin("Overflow"));
  }


  public static void verifySigned(long a, long b, boolean checkOverflow) {
    long expected = (a < b) ? 1 : 0;

    setPinSigned("InputA", a);
    setPinSigned("InputB", b);
    setPinUnsigned("Op", OpCodes.SLT);
    run();
    String message = String.format(" of %d < %d (signed) ", a, b);
    Assert.assertEquals("Output" + message, expected, readPinUnsigned("Output"));
    if (checkOverflow) {
      Assert.assertEquals("Overflow" + message, false, readPin("Overflow"));
    }
  }

  /**
   * SLTu and SLT
   */
  @Test
  public void ltSigned_allPairs() {
    long[] values = {-32768, -32767, -1, 0, 1, 32766, 32767};
    for (long a : values) {
      for (long b : values) {
        verifySigned(a, b, true);
      }
    }
  }

  public static void verifyUnsigned(long a, long b, boolean checkOverflow) {
    long expected = (a < b) ? 1 : 0;

    setPinUnsigned("InputA", a);
    setPinUnsigned("InputB", b);
    setPinUnsigned("Op", OpCodes.SLTU);
    run();
    String message = String.format(" of %d < %d (unsigned) ", a, b);
    Assert.assertEquals("Output" + message, expected, readPinUnsigned("Output"));
    if (checkOverflow) {
      Assert.assertEquals("Overflow" + message, false, readPin("Overflow"));
    }
  }

  @Test
  public void ltUnsigned_allPairs() {
    long[] values = {0, 1, 2, 16, 20, 32767, 32768, 65534, 65535};
    for (long a : values) {
      for (long b : values) {
        verifyUnsigned(a, b, true);
      }
    }
  }

  /**
   * AND, OR, NOT, XOR
   */

  private void verifyLogic(String name, int op, long a, long b, BiFunction<Long, Long, Long> func) {
    setPinUnsigned("InputA", a);
    setPinUnsigned("InputB", b);
    setPinUnsigned("Op", op);
    run();
    String message = String.format("0x%x %s 0x%x", a, name, b);
    Assert.assertEquals(message, (long)func.apply(a, b), readPinUnsigned("Output"));
    Assert.assertFalse(message + " overflow", readPin("Overflow"));
  }

  @Test
  public void testAnd() {
    verifyLogic("and", OpCodes.AND, 0xFF00, 0x0F0F, (a, b) -> a & b);
  }

  @Test
  public void testOr() {
    verifyLogic("or", OpCodes.OR,0xFF00,0x0F0F, (a,b) -> a | b );
  }

  @Test // The mask in the lambda sets bits above 16 to 0 so that Java effectively treats all results as unsigned
  public void testNot() {
    verifyLogic("not", OpCodes.NOT, 0x1, 0x0F0F, (a, b) -> (~a) & 0xFFFF);
  }

  @Test
  public void testXor() {
    verifyLogic("xor", OpCodes.XOR, 0x1, 0x0F0F, (a, b) -> (a | b) & (~a | ~b));
  }


  /**
   * Load Upper Immediate
   **/
  private void verifyLUI( long a )
  {
    setPinUnsigned("InputA", a);
    setPinUnsigned("Op", OpCodes.LUI);
    run();
    //shifting 8 bit is same as multiplying by 2^8
    Assert.assertEquals("Load Upper Immediate Output", (long) ((a*Math.pow(2,8)) % 65536), readPinUnsigned("Output"));
    Assert.assertEquals("Load Upper Immediate Overflow", false, readPin("Overflow"));
  }

  @Test
  public void LUIpairs()
  {
    long[] values = {0, 1, 2, 9,16,30, 32767, 32768};
    for(long a: values)
    {
      verifyLUI(a);
    }

  }



}
