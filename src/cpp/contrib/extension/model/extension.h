#ifndef EXTENSION_H
#define EXTENSION_H

#include <ns3/ipv4-address.h>
#include <ns3/vector.h>

#include <spdlog/fmt/fmt.h>
#include <spdlog/spdlog.h>

namespace ns3
{
Vector operator/(Vector vec, const double num);

Vector& operator/=(Vector& vec, const double num);

double dot(const Vector& a, const Vector& b);

} // namespace ns3

template <>
struct fmt::formatter<ns3::Ipv4Address> : fmt::formatter<std::string>
{
    auto format(ns3::Ipv4Address addr, format_context& ctx) const -> decltype(ctx.out())
    {
        return fmt::format_to(ctx.out(), "{:#x}", addr.Get());
    }
};

template <typename T>
struct fmt::formatter<std::vector<T>> : fmt::formatter<std::string>
{
    auto format(const std::vector<T>& data, fmt::format_context& ctx) const -> decltype(ctx.out())
    {
        auto out = ctx.out();
        fmt::format_to(out, "[");
        for (size_t i = 0; i < data.size(); ++i)
        {
            if (i != 0)
            {
                fmt::format_to(out, ", ");
            }
            fmt::format_to(out, "{}", data[i]);
        }
        fmt::format_to(out, "]");
        return out;
    }
};

#endif /* EXTENSION_H */