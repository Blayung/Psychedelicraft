/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Created by lukas on 26.04.14.
 */
public class IvNodeMatcherMethod implements IvSingleNodeMatcher
{
    public int opCode;
    public String srgMethodName;
    public String owner;
    public String desc;

    public IvNodeMatcherMethod(int opCode, String srgMethodName, String owner, String desc)
    {
        this.opCode = opCode;
        this.srgMethodName = srgMethodName;
        this.owner = owner;
        this.desc = desc;
    }

    public IvNodeMatcherMethod(int opCode, String srgMethodName, String owner, Type returnValue, Type... desc)
    {
        this(opCode, srgMethodName, owner, IvClassTransformer.getMethodDescriptor(returnValue, (Object[])desc));
    }

    @Override
    public boolean matchNode(AbstractInsnNode node)
    {
        if (node.getOpcode() != opCode)
        {
            return false;
        }

        MethodInsnNode methodInsnNode = (MethodInsnNode) node;

        if (srgMethodName != null && !srgMethodName.equals(IvClassTransformer.getSrgName(methodInsnNode)))
        {
            return false;
        }

        if (owner != null && !owner.equals(methodInsnNode.owner))
        {
            return false;
        }

        if (desc != null && !desc.equals(methodInsnNode.desc))
        {
            return false;
        }

        return true;
    }
}
