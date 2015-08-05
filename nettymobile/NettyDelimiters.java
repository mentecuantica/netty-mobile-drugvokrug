/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Содержит основные разделители фреймов
 *
 * @author Yuri Efimov
 */
public class NettyDelimiters {
    /**
     * Returns a {@code NUL (0x00)} delimiter, which could be used for
     * Flash XML socket or any similar protocols.
     */
    public static ChannelBuffer[] nulDelimiter() {
        return new ChannelBuffer[] {
                ChannelBuffers.wrappedBuffer(new byte[] { 0 }) };
    }

    /**
     * Returns {@code CR ('\r')} and {@code LF ('\n')} delimiters, which could
     * be used for text-based line protocols.
     */
    public static ChannelBuffer[] lineDelimiter() {
        return new ChannelBuffer[] {
                ChannelBuffers.wrappedBuffer(new byte[] { '\r', '\n' }),
                ChannelBuffers.wrappedBuffer(new byte[] { '\n' }),
        };
    }

    /**
     * Delimiter for ZERO ZERO bytes sequence
     * Can be bad, while working with binary data
     * @return
     */
    public static ChannelBuffer[] doubleNullDelimiter() {
        return new ChannelBuffer[] {
                ChannelBuffers.wrappedBuffer(new byte[] { 0, 0 })
        };
    }

    private NettyDelimiters() {
        // Unused
    }
}
