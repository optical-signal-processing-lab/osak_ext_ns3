#include "extension.h"

namespace ns3
{
Vector
operator/(Vector vec, const double num)
{
    Vector res;
    res.x = vec.x / num;
    res.y = vec.y / num;
    res.z = vec.z / num;
    return res;
}

Vector&
operator/=(Vector& vec, const double num)
{
    vec.x /= num;
    vec.y /= num;
    vec.z /= num;
    return vec;
}

double
dot(const Vector& a, const Vector& b)
{
    return a.x * b.x + a.y * b.y + a.z * b.z;
}
} // namespace ns3