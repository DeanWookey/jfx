/*
 * Copyright (C) 2007, 2013, 2016 Apple Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE INC. ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL APPLE INC. OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "config.h"
#include "CSSTimingFunctionValue.h"

#include <wtf/text/StringBuilder.h>

namespace WebCore {

String CSSCubicBezierTimingFunctionValue::customCSSText() const
{
    StringBuilder builder;
    builder.appendLiteral("cubic-bezier(");
    builder.appendNumber(m_x1);
    builder.appendLiteral(", ");
    builder.appendNumber(m_y1);
    builder.appendLiteral(", ");
    builder.appendNumber(m_x2);
    builder.appendLiteral(", ");
    builder.appendNumber(m_y2);
    builder.append(')');
    return builder.toString();
}

bool CSSCubicBezierTimingFunctionValue::equals(const CSSCubicBezierTimingFunctionValue& other) const
{
    return m_x1 == other.m_x1 && m_x2 == other.m_x2 && m_y1 == other.m_y1 && m_y2 == other.m_y2;
}

String CSSStepsTimingFunctionValue::customCSSText() const
{
    StringBuilder builder;
    builder.appendLiteral("steps(");
    builder.appendNumber(m_steps);
    if (m_stepAtStart)
        builder.appendLiteral(", start)");
    else
        builder.appendLiteral(", end)");
    return builder.toString();
}

bool CSSStepsTimingFunctionValue::equals(const CSSStepsTimingFunctionValue& other) const
{
    return m_steps == other.m_steps && m_stepAtStart == other.m_stepAtStart;
}

String CSSSpringTimingFunctionValue::customCSSText() const
{
    StringBuilder builder;
    builder.appendLiteral("spring(");
    builder.appendNumber(m_mass);
    builder.append(' ');
    builder.appendNumber(m_stiffness);
    builder.append(' ');
    builder.appendNumber(m_damping);
    builder.append(' ');
    builder.appendNumber(m_initialVelocity);
    builder.append(')');
    return builder.toString();
}

bool CSSSpringTimingFunctionValue::equals(const CSSSpringTimingFunctionValue& other) const
{
    return m_mass == other.m_mass && m_stiffness == other.m_stiffness && m_damping == other.m_damping && m_initialVelocity == other.m_initialVelocity;
}


} // namespace WebCore
